package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.Integrator;
import edu.stanford.integrator.ServiceStatus;
import edu.stanford.integrator.clients.IndexerRestClient;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.config.IntegrationConfig;
import edu.stanford.integrator.config.IntegrationConfigDBProxy;
import edu.stanford.integrator.importer.AbstractImporter;
import edu.stanford.integrator.repository.DatabaseService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.stanford.integrator.ServiceStatus.State.ACTIVE;
import static edu.stanford.integrator.ServiceStatus.State.INACTIVE;

@Service
public class IntegratorServiceImpl implements IntegratorService {

  private static final Logger LOGGER = Logger.getLogger(IntegratorServiceImpl.class.getName());

  private ImportedTable primaryImportedTable;

  @Autowired
  private IntegrationConfigDBProxy integrationConfigDBProxy;

  @Autowired
  DatabaseService databaseService;

  @Autowired
  LogstashJobQueueService logstashJobQueueService;

  @Autowired
  IndexerRestClient restClient;

  @Autowired
  BeanFactory beans;

  @Override
  @Transactional
  public ServiceStatus integrateTask(Integrator integrator, String remoteUser) {
    synchronized (integrator) {
      List<ImportedTable> importedTables = new ArrayList<>(); // for cleanup at the end
      try {
        // Iterate and import over all sources in the integration configuration
        String integrationID = integrator.getIntegrationID();
        final IntegrationConfig integrationConfig = readConfiguration(integrationID);
        List<String> sourceIDs = integrationConfig.getSourceIDs();
        Set<String> primaryMRNList = null;

        for (String sourceID : sourceIDs) {
          // Pretty sure this is executed Sequentially (Epic Import Code expects a cached Primarysource MRNList)

          AbstractImporter importer = ImportTargetTable(integrator, remoteUser, sourceID, primaryMRNList, importedTables);

          // This cache should only be set by the primary Data Source
          if (importer.getDataSourceConfig().isPrimarySource()) {
            // This should only be set once
            if (primaryMRNList == null) {
              primaryMRNList = importer.getMRNList();
            }
          }
        }
        // Ensure all data is available in Oracle temporary target tables before continuing
        VerifyDataAvailability(sourceIDs);

        // Acquire Primary Table is set in ImportTargetTable

        // Then, integrate + save to ElasticSearch
        String integrationSelectStatement = GenerateSqlStatement(integrator, importedTables.size(), importedTables);
        LOGGER.log(Level.INFO, "Integration select statement: \n" + integrationSelectStatement);

        // Borrow an available Logstash pool worker for this job
        LogstashPipe pipe = new LogstashPipe();
        try {
          pipe = logstashJobQueueService.borrowPipe();
          LOGGER.log(Level.INFO, "Borrowing pipe: " + pipe.getPipeName() + " and will be running in path: " + pipe.getPipeFilesystemPath());
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, "Unable to borrow logstash pipe, integration failure!");
        }

        integrator.setStatus(new ServiceStatus(ACTIVE, "Migrating to Elastic"));
        SaveToElasticsearch(integrator, remoteUser, integrationSelectStatement, pipe.getPipeNumber());

        logstashJobQueueService.returnPipe(pipe);

        integrator.setStatus(new ServiceStatus(ACTIVE, "Cleaning Up"));
        // Once everything is saved in ElasticSearch, remove the Oracle tables as they're no longer needed
        cleanup(importedTables);

      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.INFO, "ClassNotFoundException: " + e);
        cleanup(importedTables);
        integrator.setStatus(new ServiceStatus(INACTIVE, e));
      } catch (Exception e) {
        LOGGER.log(Level.INFO, "ClassNotFoundException: " + e);
        cleanup(importedTables);
        integrator.setStatus(new ServiceStatus(INACTIVE, e));
      }
    }
    String timeStampString = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString();
    integrator.setStatus(new ServiceStatus(INACTIVE, INTEGRATION_SUCCESS + " " + timeStampString));

    return integrator.getStatus();
  }

  private AbstractImporter ImportTargetTable(Integrator integrator, String remoteUser, String sourceID, Set<String> primaryMRNList, List<ImportedTable> importedTables) throws Exception {
    String integrationID = integrator.getIntegrationID();
    final IntegrationConfig integrationConfig = readConfiguration(integrationID);
    int numberOfSources = integrationConfig.getSourceIDs().size();
    LOGGER.log(Level.INFO, "Importing sources from configuration: " + integrationID + "  ...");
    int sourceIndex = Integer.valueOf(sourceID) + 1;

    integrator.setStatus(new ServiceStatus(ACTIVE, "Importing "
            + integrationConfig.getSourceConfig(sourceID).getSourceName() + " from " + integrationConfig.getSourceConfig(sourceID).getSourceType()
            + " (source " + sourceIndex + " out of " + numberOfSources + ") ..."));

    LOGGER.log(Level.INFO, "Importing source_id: " + sourceID + " ...");
    DataSourceConfig sourceConfig = integrationConfig.getSourceConfig(sourceID);

    // importing the source data
    String sourceType = sourceConfig.getSourceType().toLowerCase();
    String sourceImporterClass = "edu.stanford.integrator.importer." + sourceType.toUpperCase() + "Importer";
    String importTargetTable = integrationID + "_S" + sourceID;
    String importTargetTableAlias = "S" + sourceID;

    LOGGER.log(Level.INFO, "Import Target Table Name: " + importTargetTable + " referenced as alias: " + importTargetTableAlias);
    AbstractImporter importer = (AbstractImporter) beans.getBean(Class.forName(sourceImporterClass));

    Set<String> createdTables = importer.importData(importTargetTable, sourceConfig, remoteUser, primaryMRNList);
    LOGGER.log(Level.INFO, "Number of created tables:" + createdTables.size());

    for (String table : createdTables) {
      ImportedTable importedTable = new ImportedTable(table, sourceConfig);
      if (sourceConfig.isPrimarySource()) {
        this.primaryImportedTable = importedTable;
      }
      importedTables.add(importedTable);
      LOGGER.log(Level.INFO, "Created Target Table Name: " + importedTable.getName());
    }

    return importer;
  }

  private String GenerateSqlStatement(Integrator integrator, int numberOfSources, List<ImportedTable> importedTables) throws Exception {
    String integrationID = integrator.getIntegrationID();
    final IntegrationConfig integrationConfig = readConfiguration(integrationID);
    final DataSourceConfig primarySourceConfig = integrationConfig.getSourceConfig("0");
    ImportedTable primaryImportedTable = importedTables.get(0);

    if (numberOfSources <= 0) {
      LOGGER.log(Level.SEVERE, "NO SOURCES TO INTEGRATE, CANNOT CREATE SQL STATEMENT");
      throw new Exception("NO SOURCES TO INTEGRATE, CANNOT CREATE SQL STATEMENT");
    }
    if (numberOfSources == 1) {
      // No need for intermediatary tables, return S0 by itself with index position column
      return "select S0.*, ROW_NUMBER() OVER (ORDER BY " + primaryImportedTable.getName() + "_join_id_mrn) AS RowNumber from " + importedTables.get(0).getName() + " S0";

    } else if (numberOfSources == 2) {
      // No need for intermediate tables, join S0 to S1
      String finalSelect = "";
      final DataSourceConfig secondarySourceConfig = integrationConfig.getSourceConfig("1");
      ImportedTable nonPrimaryImportedTable = importedTables.get(1);
      String joinIdSuffix = nonPrimaryImportedTable.getJoinIdSuffix(); // e.g. _join_id_ppid. Both primary and nonprimary should use nonprimary connectAs suffix
      String joinIdPrimary = primaryImportedTable.getName() + joinIdSuffix;
      String joinIdNonPrimary = nonPrimaryImportedTable.getName() + joinIdSuffix;

      String baseSql = "select S0.*, S1.*, ROW_NUMBER() OVER (ORDER BY " + joinIdPrimary + ") AS RowNumber from " + primaryImportedTable.getName() + " S0\n" +
              "left join " + nonPrimaryImportedTable.getName() + " S1\n" +
              "on S0." + joinIdPrimary + " = S1." + joinIdNonPrimary;

      // Append a where clause for exact matches on visit ID info.
      // use connections from secondary for the primary
      String primaryVisitIdField = primarySourceConfig.getVisitIdColumn(secondarySourceConfig.getVisitIdFieldFromConnections());
      LOGGER.log(Level.INFO, "primaryVisitIdField (if exists): " + primaryVisitIdField);
      String nonPrimaryVisitIdField = secondarySourceConfig.getVisitIdColumn(secondarySourceConfig.getVisitIdFieldFromConnections());
      LOGGER.log(Level.INFO, "nonPrimaryVisitIdField (if exists): " + nonPrimaryVisitIdField);

      // Add date clause if needed as defined in the sourceConfig, otherwise DateClause is an empty string and check for generic Visit ID
      String DateClause = AppendDateMatchingIfNeeded(nonPrimaryImportedTable, "S1");
      if (!DateClause.equals("")) {
        LOGGER.log(Level.INFO, "Appending Visit Date 'where' clause to SQL statement");
        finalSelect = baseSql + DateClause;
      }
      // Generic Visit ID
      else if (primaryVisitIdField != null && !primaryVisitIdField.equals("") && nonPrimaryVisitIdField != null && !nonPrimaryVisitIdField.equals("")) {
        primaryVisitIdField = primaryVisitIdField.replaceAll(" ", "_").toLowerCase();
        LOGGER.log(Level.INFO, "Appending Generic Visit ID 'where' clause to SQL statement");
        String appendVisitIdSql = "\n" +
                "where S0." + primaryImportedTable.getName() + "_" + primaryVisitIdField + " = S1." + nonPrimaryImportedTable.getName() + "_join_visit_id";
        finalSelect = baseSql + appendVisitIdSql;
      } else {
        // No visit ID info
        LOGGER.log(Level.INFO, "Not appending any Visit Date/ID 'where' clauses to SQL statement");
        finalSelect = baseSql;
      }
      return finalSelect;

    } else {
      String createStatement = "";
      // Create intermediary tables for the total number of sources minus one, as the last select statement
      // will be a merged S0 with the final source
      // i = 1 starting with S1

      // Keep track of the last merge table for the final select statement to use as S0
      String lastMergeTable = "";

      String VisitIDClause = "";
      // Keep track of the current value to not bump iterator
      int CurrentValue = 0;
      // Capture iterator for final Select Statement
      int FinalValue = 1;
      // Track how many merge tables are created, so we can select the last source prior to any merge tables
      int MergeTables = 0;

      for (int i = 1; i < (numberOfSources - 1); i++) {
        ImportedTable nonPrimaryImportedTable = importedTables.get(i);
        // create merged table
        CurrentValue++;
        MergeTables++;
        String targetAlias = nonPrimaryImportedTable.getSqlAlias(); // e.g. S1
        // Use the nonprimary connections info to determine how to connect to primary for visit ID purposes
        String primaryVisitIdField = primaryImportedTable.getVisitIdColumn(nonPrimaryImportedTable.getVisitIdFieldFromConnections());
        String joinIdSuffix = nonPrimaryImportedTable.getJoinIdSuffix(); // e.g. _join_id_ppid. For use with both primary and nonprimary
        String joinIdPrimary = primaryImportedTable.getName() + joinIdSuffix;
        String joinIdNonPrimary = nonPrimaryImportedTable.getName() + joinIdSuffix;
        String mergedTable = "merged_" + integrationID + "_" + Integer.toString(CurrentValue); // e.g. merged_j20190326104413_2
        String previousMergedTable = "merged_" + integrationID + "_" + Integer.toString(CurrentValue - 1); // e.g. merged_j20190326104413_1

        // Drop table if it exists
        //                LOGGER.log(Level.INFO, "Dropping temporary merge table : " + mergedTable);
        //                String dropStatement = "drop table " + mergedTable;
        //                databaseService.executeSqlQuery(dropStatement);

        // First run uses S0 as the base, from then on use merge tables
        if (i == 1) {
          createStatement =
                  "create table " + mergedTable + " AS (\n" +
                          "select S0.*, " + targetAlias + ".*  from " + importedTables.get(0).getName() + " S0\n" +
                          "left join " + importedTables.get(i).getName() + " " + targetAlias + "\n" +
                          "on S0." + joinIdPrimary + " = " + targetAlias + "." + joinIdNonPrimary;
        } else {
          createStatement =
                  "create table " + mergedTable + " AS (\n" +
                          "select S0.*, " + targetAlias + ".*  from " + previousMergedTable + " S0\n" +
                          "left join " + importedTables.get(i).getName() + " " + targetAlias + "\n" +
                          "on S0." + joinIdPrimary + " = " + targetAlias + "." + joinIdNonPrimary;
        }

        // Add date clause if needed as defined in the sourceConfig, otherwise DateClause is an empty string and check for generic Visit ID
        String DateClause = AppendDateMatchingIfNeeded(nonPrimaryImportedTable, targetAlias);
        if (!DateClause.equals("")) {
          LOGGER.log(Level.INFO, "Appending Visit Date 'where' clause to SQL statement");
          createStatement += DateClause;
        }

        // Generic Visit ID
        else if (primaryVisitIdField != null && !primaryVisitIdField.equals("") && nonPrimaryImportedTable.getVisitIdFieldFromConnections() != null && !nonPrimaryImportedTable.getVisitIdFieldFromConnections().equals("")) {
          primaryVisitIdField = primaryVisitIdField.replaceAll(" ", "_").toLowerCase();
          LOGGER.log(Level.INFO, "Appending Generic Visit ID 'where' clause to SQL statement");
          VisitIDClause = "\n" +
                  "where S0." + primaryImportedTable.getName() + "_" + primaryVisitIdField + " = " + targetAlias + "." + nonPrimaryImportedTable.getName() + "_join_visit_id";
          createStatement += VisitIDClause;
        } else {
          // No visit ID info
          LOGGER.log(Level.INFO, "Not appending any Visit Date/ID 'where' clauses to SQL statement for this merged table");
        }
        // Finish the merged table creation
        createStatement += ")";

        // execute to create the temp merged table with databaseService
        databaseService.executeSqlQuery(createStatement);
        lastMergeTable = mergedTable;
        // append this table to the list to clean up
        ImportedTable importedMergeTable = new ImportedTable(mergedTable, primaryImportedTable.getConfig());
        importedTables.add(importedMergeTable);
        FinalValue++;
      }

      ImportedTable lastTable = importedTables.get(importedTables.size() - 1 - MergeTables);

      // Final select statement uses S0 of merged tables with the final source
      String joinIdSuffix = lastTable.getJoinIdSuffix(); // e.g. _join_id_ppid. For use with both primary and nonprimary

      String joinIdPrimary = primaryImportedTable.getName() + joinIdSuffix;
      String joinIdNonPrimary = importedTables.get(FinalValue).getName() + joinIdSuffix;

      String finalSelect =
              "select S0.*, S" + FinalValue + ".*, ROW_NUMBER() OVER (ORDER BY " + joinIdPrimary + ") AS RowNumber from " + lastMergeTable + " S0\n" +
                      "left join " + importedTables.get(FinalValue).getName() + " S" + FinalValue + "\n" +
                      "on S0." + joinIdPrimary + " = S" + FinalValue + "." + joinIdNonPrimary;

      String FinalVisitIDClause = "";
      ImportedTable nonPrimaryImportedTable = importedTables.get(FinalValue);
      String primaryVisitIdField = primaryImportedTable.getVisitIdColumn(nonPrimaryImportedTable.getVisitIdFieldFromConnections());
      // Also check date clause if needed on the last table, since we never check the last table as part of the loop
      // Add date clause if needed as defined in the sourceConfig, otherwise DateClause is an empty string and check for generic Visit ID
      String LastDateClause = AppendDateMatchingIfNeeded(lastTable, "S" + FinalValue);
      if (!LastDateClause.equals("")) {
        finalSelect += LastDateClause;
      } else if (primaryVisitIdField != null && !primaryVisitIdField.equals("") &&
              nonPrimaryImportedTable.getVisitIdFieldFromConnections() != null &&
              !nonPrimaryImportedTable.getVisitIdFieldFromConnections().equals("")) {
        primaryVisitIdField = primaryVisitIdField.replaceAll(" ", "_").toLowerCase();
        FinalVisitIDClause = "\n" +
                "where S0." + primaryImportedTable.getName() + "_" + primaryVisitIdField + " = S" + FinalValue + "." + nonPrimaryImportedTable.getName() + "_join_visit_id";
        finalSelect += FinalVisitIDClause;
      } else {
        LOGGER.log(Level.INFO, "Not appending any Visit ID 'where' clauses to SQL statement for this final merged table");
      }
      return finalSelect;
    }
  }

  private String AppendDateMatchingIfNeeded(ImportedTable importedTable, String alias) throws Exception {
    String dateColumn = "join_visit_date";
    String dateTableRef = alias + "." + importedTable.getName() + "_" + dateColumn;

    try {
      LOGGER.log(Level.INFO, "This imported table visit id type: " + importedTable.getVisitIdType());
    } catch (NullPointerException ex) {
      LOGGER.log(Level.INFO, importedTable.getName() + " has no visit ID type specified.");
      return "";
    }

    if (importedTable.getVisitIdType() != null && importedTable.getVisitIdType().equals("visitDate")) {
      String DateMatchingClause = "\n" +
              "where " + dateTableRef + " IS NOT NULL" + "\n" +
              "AND TO_DATE(" + dateTableRef + ", 'YYYY-MM-DD HH24:MI:SS') - TO_DATE(" + dateTableRef + ", 'YYYY-MM-DD HH24:MI:SS') <= " + Integer.valueOf(importedTable.getDateMatchingRangeInDays()) + "\n" +
              "AND TO_DATE(" + dateTableRef + ", 'YYYY-MM-DD HH24:MI:SS') - TO_DATE(" + dateTableRef + ", 'YYYY-MM-DD HH24:MI:SS') >= 0";

      LOGGER.log(Level.INFO, "This imported table source: " + importedTable.getName() + " requires Event Date Matching, appending to SQL query");
      return DateMatchingClause;
    }
    LOGGER.log(Level.INFO, "No Visit Date Matching necessary for this integration");
    return "";
  }

  private void SaveToElasticsearch (Integrator integrator, String remoteUser, String integrationSelectStatement, String pipelineId) throws Exception {
    String integrationID = integrator.getIntegrationID();
    final String integrationName =  integrationID;

    try {
      String elasticsearchIndexName = integrationName;

      reindex(integrator, integrationSelectStatement, elasticsearchIndexName, pipelineId);
      //this.reindex(integrationSelectStatement, elasticsearchIndexName);
      LOGGER.log(Level.INFO, "SUCCESS: able to save integration table in ElasticSearch as: " + elasticsearchIndexName);
    } catch (Exception ex) {
      integrator.setStatus(new ServiceStatus(INACTIVE, "Indexing in ElasticSearch failed. " + ex.getLocalizedMessage()));
      throw new Exception("Indexing in ElasticSearch failed. " + ex.getLocalizedMessage());
    }

    String timeStampString = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString();
    integrator.setStatus(new ServiceStatus(INACTIVE, INTEGRATION_SUCCESS + " " + timeStampString));
  }


  private IntegrationConfig readConfiguration(String configID) throws Exception {

    final JsonNode configJson = integrationConfigDBProxy.getIntegrationConfig(configID);

    if (configJson == null) {
      LOGGER.log(Level.SEVERE, "The configuration for integration " + configID + " is missing.");
      throw new Exception("The configuration for integration " + configID + " is missing.");
    }

    return new IntegrationConfig(configJson);
  }

  // TODO put in a separate service
  private void reindex (Integrator integrator, String selectStatement, String indexName, String pipelineId) throws Exception {
    LOGGER.log(Level.INFO, "Reindexing the new integration table...");
    selectStatement = selectStatement.replace("\n"," ");
    selectStatement = selectStatement.replace(";","");
    selectStatement = selectStatement.replaceAll("\"","\\\\\"");
    LOGGER.log(Level.INFO, "Escaped SQL select statement: " + selectStatement);

    Map<String, Object> result = restClient.index(selectStatement, indexName, pipelineId, true);

    String respCode = (String) result.get(IndexerRestClient.RESPONSE_CODE);
    if (!respCode.equals("200")) {
      integrator.setStatus(new ServiceStatus(INACTIVE, (String) result.get(IndexerRestClient.RESPONSE_BODY)));
      throw new Exception("Indexer error: " + respCode + "\n" + result.get(IndexerRestClient.RESPONSE_BODY));
    }
    String id = (String) result.get(IndexerRestClient.RESPONSE_BODY);
    Map<String, Object> status = restClient.getIndex(id);
    String statusBody = (String) status.get(IndexerRestClient.RESPONSE_BODY);
    while (statusBody.contains("WAITING")) {
      status = restClient.getIndex(id);
      statusBody = (String) status.get(IndexerRestClient.RESPONSE_BODY);
    }
    if (statusBody.contains("ERROR")) {
      throw new Exception(statusBody);
    }
  }

  private void cleanup(List<ImportedTable> importedTables) {

    LOGGER.log(Level.INFO, "Cleaning up...");

    for (ImportedTable importedTable : importedTables) {

      LOGGER.log(Level.INFO, "Removing imported table: " + importedTable.getName());
      try {
        databaseService.executeSqlDrop(importedTable.getName());
      } catch (InvalidDataAccessResourceUsageException e) {
        LOGGER.log(Level.INFO, "Cannot drop " + importedTable + ". Table does not exist.");
      }
    }
    importedTables.clear();
  }

  private void VerifyDataAvailability(List<String> integrationConfig) throws Exception {
    LOGGER.log(Level.INFO, "TODO: Add a step here to make sure that all data is present in temp tables prior to integration.");
  }
}
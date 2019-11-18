package edu.stanford.integrator.importer;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.repository.DatabaseService;
import edu.stanford.integrator.services.PreprocessorService;
import edu.stanford.integrator.services.TokenGrowthService;
import edu.stanford.integrator.transform.visitdate.VisitDateTransformService;
import edu.stanford.integrator.transform.visitid.VisitIdTransformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ImporterUtil {
  @Autowired
  DatabaseService databaseService;

  @Autowired
  PreprocessorService preprocessorService;

  @Autowired
  TokenGrowthService tokenGrowthService;

  @Autowired
  VisitIdTransformService visitIdTransformService;

  @Autowired
  VisitDateTransformService visitDateTransformService;

  private static final Logger LOGGER = Logger.getLogger(ImporterUtil.class.getName());

  public void insertData(String importTableName, JsonNode dataToImport, DataSourceConfig config) throws Exception {
    String tableSchema = constructTableSchema(dataToImport, config, importTableName);
    if (tableSchema == null || tableSchema.equals("")) {
      String message = "Unable to create the table schema";
      LOGGER.log(Level.INFO, message);
      throw new Exception(message);
    }

    String sqlCreate = "CREATE TABLE IF NOT EXISTS "
        + importTableName
        + "  (" + tableSchema + ")";

    String sqlEmpty = "DELETE FROM " + importTableName;

    LOGGER.log(Level.INFO, sqlCreate);
    try {
      databaseService.executeSqlQuery(sqlCreate);
      databaseService.executeSqlQuery(sqlEmpty);

    } catch (Exception e) {
      String message = "Exception executing sql: " + sqlCreate + "\n" + e.getMessage();
      LOGGER.log(Level.SEVERE, message);
      throw new Exception(message);
    }
    List<String> insertStatements = new ArrayList<>();
    for (JsonNode row : dataToImport) {
      insertStatements.add(createInsertStatement(importTableName, row, config));
    }

    for (String insertStatement : insertStatements) {
      try {
        databaseService.executeSqlQuery(insertStatement);
        databaseService.executeSqlQuery("COMMIT WORK");
      } catch (Exception e) {
        String message = "sql: " + insertStatement + " Error in one of the insert statements. " + e.getMessage();
        LOGGER.log(Level.INFO, message);
        throw new Exception(message);
      }
    }
/*
		try {
			databaseService.executeSqlDrop(importTableName);
		} catch (InvalidDataAccessResourceUsageException e) {
			LOGGER.log(Level.INFO, "Cannot drop " + importTableName + ". Table does not exist.");
		}
*/
  }

  public String constructTableSchema(JsonNode tableRows, DataSourceConfig config, String importTableName) throws Exception {

    if (tableRows == null) {
      throw new IllegalArgumentException("constructTableSchema: tableRows is Nul");
    }

    //String tableSchema = "";
    // Making sure that every imported table has at least one column with unique values. This is to make sure that the primary specimen table has such a column, which is a requirement by Indexer.
    // trying to name this column in a unique way to avoid naming collisions with the impoted column names...: datasource__import__index
    //TODO: Do we still need this with how the SQL now works? I think this results in only 1 row returned based on how the UI treats biocatalyst-id. The records are then all the same biocatalyst-id j123456789012%{{s0_datasource_import_index}}
    String tableSchema = config.getSqlAlias() + "_datasource__import__index INTEGER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1), ";

    JsonNode firstRow = tableRows.get(0);
    if (firstRow == null) {
      throw new Exception("No records to import");
    }

    Iterator<String> fieldsIterator = firstRow.fieldNames();
    if (fieldsIterator.hasNext() == false) {
      throw new Exception("Records are empty rows. no fields.");
    }

    while (fieldsIterator.hasNext()) {

      String fieldName = fieldsIterator.next();

      // Limit how big field names as database limits require this
      final int MAX_COLUMN_NAME_LENGTH = 44;
      fieldName = (fieldName.length() >= MAX_COLUMN_NAME_LENGTH) ? fieldName.substring(0, MAX_COLUMN_NAME_LENGTH) : fieldName;

      fieldName = this.curateFieldName(fieldName, config.getSourceId(), importTableName);

      tableSchema += fieldName + " VARCHAR(4000), ";
    }

    // Don't add a "curated" prefix with curateFieldName!
    // We're creating both PPID and MRN join_id fields. S0 will populate both. S1 only populates one.
    tableSchema += importTableName + "_join_id_mrn VARCHAR(4000), ";
    tableSchema += importTableName + "_join_id_ppid VARCHAR(4000), ";
    // may or may not be filled in, depending if visit date exists in a given source
    tableSchema += importTableName + "_join_visit_date VARCHAR(4000), ";
    // only filled in for nonprimary sources
    tableSchema += importTableName + "_join_visit_id VARCHAR(4000) ";

    return tableSchema;
  }

  public String createInsertStatement(String importTableName, JsonNode row, DataSourceConfig config) throws Exception {
    // TODO: This should be it's own importer service
    if (importTableName == null || importTableName.equals("") || row == null) {
      throw new IllegalArgumentException("createInsertStatement: importTableName name or row are null or empty");
    }

    StringBuilder columns = new StringBuilder("(");
    StringBuilder values = new StringBuilder("(");
    String transformedIdMrn = "";
    String transformedIdPpid = "";
    String transformedVisitId = "";
    String transformedVisitDate = "";
    String connectsAs = config.getConnectToPrimaryIdType();

    Iterator<Map.Entry<String, JsonNode>> fieldIterator = row.fields();
    while (fieldIterator.hasNext()) {

      Map.Entry<String, JsonNode> currentField = fieldIterator.next();
      //LOGGER.log(Level.INFO, "Adding field to insert statement: " + currentField.getKey() +  ":" + currentField.getValue() + "|");
      String fieldName = currentField.getKey();

      // Limit how big field names as database limits require this.
      final int MAX_COLUMN_NAME_LENGTH = 44;
      fieldName = (fieldName.length() >= MAX_COLUMN_NAME_LENGTH) ? fieldName.substring(0, MAX_COLUMN_NAME_LENGTH) : fieldName;

      fieldName = this.curateFieldName(fieldName, config.getSourceId(), importTableName);

      columns.append(fieldName).append(", ");

      String value = currentField.getValue().toString();
      //LOGGER.log(Level.INFO, "Value: " + value);
      if (value == null || value.equals("") || value.equals("\"\"")) {
        value = "null";
      }

      // VISIT DATE TRANSFORM
      // Primary and non-primary follow the same strategy for transforming Visit Date
      // Everything is converted to system time
      if (config.getVisitIdColumn("visitDate") != null) {
        String visitDateField = config.getVisitIdColumn("visitDate");
        String curatedVisitDateFieldName = curateFieldName(visitDateField, config.getSourceId(), importTableName);
        if (fieldName.equals(curatedVisitDateFieldName)) {
          LOGGER.log(Level.INFO, "This is a Visit Date field, calling visitDateTransformService");
          visitDateTransformService.setDataSourceConfig(config);
          transformedVisitDate = wrapInSingleQuotes(visitDateTransformService.transform(sanitizeSpecialChars(value)));
        }
      }

      // VISIT ID TRANSFORM
      // primary
      // No transform necessary; Visit ID transforms all non-primary sources to the primary (map)

      // non-primary
      // getVisitIdConnectionsSource will be null for primary

      if (config.getVisitIdConnectionsSource() != null &&
          config.getVisitIdType() != null) {
        String visitIdField = config.getVisitIdColumn(config.getVisitIdType());
        String curatedVisitIdFieldName = curateFieldName(visitIdField, config.getSourceId(), importTableName);
        if (fieldName.equals(curatedVisitIdFieldName)) {
          LOGGER.log(Level.INFO, "This is a Visit ID field, calling visitIdTransformService");
          visitIdTransformService.setDataSourceConfig(config);
          transformedVisitId = wrapInSingleQuotes(visitIdTransformService.transform(sanitizeSpecialChars(value)));
        }
      }

      // PATIENT IDENTIFIER (PPID) TRANSFORM
      // If this value is for the integration field, we need to apply the PreprocessorService
      // We only apply this logic to nonprimary sources, as the primary requires no manipulation (for now).
      // We need to know how the source connects to primary to ensure we're manipulating the correct ID column (connectsAs)

      // primary
      if (config.getIdColumnNameMrn() != null) {
        if (fieldName.equals(curateFieldName(config.getIdColumnNameMrn(), config.getSourceId(), importTableName))) {
          transformedIdMrn = preprocessorService.transformId(config, value, "MRN", true, null);
        }
      }
      if (config.getIdColumnNamePpid() != null) {
        if (fieldName.equals(curateFieldName(config.getIdColumnNamePpid(), config.getSourceId(), importTableName))) {
          if (tokenGrowthService.detectTokenGrowth(config, value)) {
            LOGGER.log(Level.INFO, "Token growth detected for this value compared against against the processed_id. Calling TokenGrowthService.");
            // .get(0) means this only works if there is one token in `growth_tokens` in the config, which is all that is currently supported
            List<Integer> tokenGrowthDelimiterPositions = tokenGrowthService.correctedDelimiterPositions(config, config.getGrowthTokens("ppid").get(0), config.getDelimiterPositions("ppid"), value);
            transformedIdPpid = preprocessorService.transformId(config, value, "PPID", true, tokenGrowthDelimiterPositions);
          } else {
            transformedIdPpid = preprocessorService.transformId(config, value, "PPID", true, null);
          }
        }
      }

      // non-primary
      if (config.getIdColumnNameMrn() != null && connectsAs != null && connectsAs.equalsIgnoreCase("MRN")) {
        if (fieldName.equals(curateFieldName(config.getIdColumnNameMrn(), config.getSourceId(), importTableName))) {
          transformedIdMrn = preprocessorService.transformId(config, value, "MRN", true, null);
        }
      }
      if (config.getIdColumnNamePpid() != null && connectsAs != null && connectsAs.equalsIgnoreCase("PPID")) {
        // if there's token growth, determine the appropriate offset and pass the corrected tokenGrowthDelimiterPositions
        if (fieldName.equals(curateFieldName(config.getIdColumnNamePpid(), config.getSourceId(), importTableName))) {
          if (tokenGrowthService.detectTokenGrowth(config, value)) {
            LOGGER.log(Level.INFO, "Token growth detected for this value compared against against the processed_id. Calling TokenGrowthService.");
            // .get(0) means this only works if there is one token in `growth_tokens` in the config, which is all that is currently supported
            List<Integer> tokenGrowthDelimiterPositions = tokenGrowthService.correctedDelimiterPositions(config, config.getGrowthTokens("ppid").get(0), config.getDelimiterPositions("ppid"), value);
            transformedIdPpid = preprocessorService.transformId(config, value, "PPID", true, tokenGrowthDelimiterPositions);
          } else {
            transformedIdPpid = preprocessorService.transformId(config, value, "PPID", true, null);
          }
        }
      }
      values.append(curateValue(value)).append(", ");
    }
    // Since join_id fields are at the end, append the transformedIdMrn, then transformedIdPpid last
    if (connectsAs.equalsIgnoreCase("MRN") && (transformedIdMrn == null || transformedIdMrn.equals(""))) {
      throw new Exception("ERROR! Trying to connect w/ MRN and transformedIdMrn is null, meaning the join_id column for this source will be empty and integration will fail!");
    }
    if (connectsAs.equalsIgnoreCase("PPID") && (transformedIdPpid == null || transformedIdPpid.equals(""))) {
      throw new Exception("ERROR! Trying to connect w/ PPID and transformedIdPpid is null, meaning the join_id column for this source will be empty and integration will fail!");
    }

    // Add value to join_visit_date, if it exists
    if (!transformedVisitDate.equals("")) {
      values.append(transformedVisitDate).append(", ");
    } else {
      values.append("'null'").append(", ");
    }
    columns.append(importTableName + "_join_visit_date, ");

    // Add value to join_visit_id, if it exists
    if (!transformedVisitId.equals("")) {
      values.append(transformedVisitId).append(", ");
    } else {
      values.append("'null'").append(", ");
    }
    columns.append(importTableName + "_join_visit_id, ");

    // Add the join_id columns as the last  part of the insert statement, either the value or null if it's null
    if (!transformedIdMrn.equals("")) {
      values.append(curateValue(transformedIdMrn)).append(", ");
    } else {
      values.append("'null'").append(", ");
    }
    columns.append(importTableName + "_join_id_mrn, ");

    if (!transformedIdPpid.equals("")) {
      values.append(curateValue(transformedIdPpid)).append(", ");
    } else {
      values.append("'null'").append(", ");
    }
    columns.append(importTableName + "_join_id_ppid, ");

    // remove extra "," and add the closing braces
    columns.replace(columns.length() - 2, columns.length(), ")");
    values.replace(values.length() - 2, values.length(), ")");
    String valuesWithoutDoubleQuotes = values.toString().replace('\"', '\'');

    String sqlInsertSttmt = "INSERT INTO " + importTableName + " " + columns.toString() + " VALUES " + valuesWithoutDoubleQuotes;

    //LOGGER.log(Level.INFO, "Built insert statement: " + sqlInsertSttmt);
    return sqlInsertSttmt;
  }

  private String curateValue(String value) {
    if (value.length() > 4000) {
      value = value.substring(0, 4000);
      value = value.concat("\"");
    }

    // Escaping single quotes
    String valueEscapedApostrophe = value.replace("'", "''");
    String trimmed = valueEscapedApostrophe.trim();
    return trimmed;
  }

  private String wrapInSingleQuotes(String value) {
    return "'" + value + "'";
  }

  private String sanitizeSpecialChars(String value) {
    // replace all non alphanumeric characters with underscore
    return value.replaceAll("[^a-zA-Z0-9_]", "_");
  }

  private String curateFieldName(String fieldName, String sourceId, String importTableName) {
    if (fieldName == null || fieldName.equals("")) {
      return null;
    }

    //Remove empty space in the field name
    String curatedFieldName = fieldName.replaceAll("\\s", "_");

    //Making the field unique across the various datasources that will be added to the same table
    return sanitizeSpecialChars(importTableName + "_" + curatedFieldName);
  }

  public Set<String> ExtractUniqueColumnDataFromJsonNodeArray(JsonNode table, String ColumnName) throws Exception {
    List<String> result = ExtractColumnDataFromJsonNodeArray(table, ColumnName);
    return new HashSet<String>(result);
  }

  public List<String> ExtractColumnDataFromJsonNodeArray(JsonNode table, String ColumnName) throws Exception {
    if (table == null) {
      LOGGER.log(Level.SEVERE, "Could not Extract Column Data: Table is Null");
      throw new Exception();
    }

    if (!table.isArray()) {
      LOGGER.log(Level.SEVERE, "Could not Extract Column Data: Table is not an array");
      throw new Exception();
    }

    if (ColumnName == null ||
        ColumnName.equals("")) {
      LOGGER.log(Level.SEVERE, "Could not Extract Column Data: ColumnName not set to a usable value.");
      throw new Exception();
    }
		
		/*
			LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 0:" + table);
			LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 1 - ColumnName: " + ColumnName);
			LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 2 - isArray: " + table.isArray());
		*/
    List<String> data = new LinkedList<String>();
    for (final JsonNode row : table) {
	    	/*
		    	LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 2 - row: " + row);
		    	LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 2 - get: " + row.get(ColumnName));
		    	LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 2 - toString: " + row.get(ColumnName).toString());
		    	LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 2 - textValue: " + row.get(ColumnName).textValue());
	    	*/
      String mrn = row.get(ColumnName).textValue();
      data.add(mrn);
    }
	    /*
			LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 3:" + data);
			LOGGER.log(Level.INFO, "ExtractColumnDataFromJsonNodeArray 4:" + data.toString());
		*/
    return data;
  }

}

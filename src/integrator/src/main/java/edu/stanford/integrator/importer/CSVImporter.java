package edu.stanford.integrator.importer;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.clients.LocalStorageClient;
import edu.stanford.integrator.config.DataSourceConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CSVImporter extends AbstractImporter {

  private static final Logger LOGGER = Logger.getLogger(CSVImporter.class.getName());

  @Autowired
  ImporterUtil importerUtil;

  @Autowired
  LocalStorageClient storage;

  @Override
  protected Set<String> importDataImpl(String importTableName, DataSourceConfig config, String remoteUser, Set<String> MRNList) throws Exception {
    JsonNode dataToImport = null;

    try {
      String id = config.getCsvStorageID();
      dataToImport = storage.getData(id, remoteUser);
      // Not yet supported
      //this.mrnList = importerUtil.ExtractUniqueColumnDataFromJsonNodeArray(dataToImport, config.getIdColumnNameMrn());

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Invalid CSV data format: \n" + config.getData(), ex);
      throw new Exception("Invalid CSV data format: \n" + config.getData());
    }

    try {
      importerUtil.insertData(importTableName, dataToImport, config);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error while importing CSV data. " + ex.getMessage());
      throw new Exception("Error while importing CSV data. " + ex.getMessage());
    }

    // Returning List of created tables
    Set<String> createdTables = new LinkedHashSet<String>();
    createdTables.add(importTableName);
    return createdTables;
  }

}


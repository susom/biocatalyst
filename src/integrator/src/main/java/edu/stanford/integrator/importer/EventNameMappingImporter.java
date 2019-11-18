package edu.stanford.integrator.importer;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.DataSourceConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventNameMappingImporter extends AbstractImporter {

  private static final Logger LOGGER = Logger.getLogger(EventNameMappingImporter.class.getName());

  @Autowired
  ImporterUtil importerUtil;

  @Override
  protected Set<String> importDataImpl(String tableName, DataSourceConfig config, String remoteUser, Set<String> MRNList) throws Exception {
    JsonNode dataToImport = null;
    try {
      dataToImport = config.getEventNameMapping().get("rows");
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Invalid Event Name Mapping format: \n" + dataToImport, ex);
      throw new Exception("Invalid Event Name Mapping format: \n" + dataToImport);
    }
    try {
      importerUtil.insertData(tableName, dataToImport, config);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error while importing Event Name Mapping. " + ex.getLocalizedMessage());
      throw new Exception("Error while importing Event Name Mapping. " + ex.getLocalizedMessage());
    }

    // Returning List of created tables
    Set<String> createdTables = new LinkedHashSet<String>();
    createdTables.add(tableName);
    return createdTables;
  }

}

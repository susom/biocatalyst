package edu.stanford.integrator.importer;

import edu.stanford.integrator.config.DataSourceConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCImporter extends AbstractImporter {

  private static final Logger LOGGER = Logger.getLogger(JDBCImporter.class.getName());
  private final DataSourceConfig config;

  public JDBCImporter(DataSourceConfig sourceConfig) {
    this.config = sourceConfig;
  }

  @Override
  protected Set<String> importDataImpl(String tableName, DataSourceConfig config, String remoteUser, Set<String> MRNList) throws Exception {
    LOGGER.log(Level.INFO, "JDBCTableImporter:importData() - Not supported yet.");

    // Returning List of created tables
    Set<String> createdTables = new LinkedHashSet<String>();
    createdTables.add(tableName);
    return createdTables;
  }

}

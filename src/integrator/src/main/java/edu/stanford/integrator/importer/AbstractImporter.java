package edu.stanford.integrator.importer;

import edu.stanford.integrator.config.DataSourceConfig;
import java.util.Set;

public abstract class AbstractImporter {
  protected Set<String> mrnList = null;
  protected DataSourceConfig sourceConfig = null;

  public final Set<String> importData(String importTableName, DataSourceConfig sourceConfig, String remoteUser, Set<String> MRNList) throws Exception {
    this.sourceConfig = sourceConfig;
    return this.importDataImpl(importTableName, sourceConfig, remoteUser, MRNList);
  }

  protected abstract Set<String> importDataImpl(String importTableName, DataSourceConfig sourceConfig, String remoteUser, Set<String> MRNList) throws Exception;

  public final Set<String> getMRNList() {
    return mrnList;
  }

  public final DataSourceConfig getDataSourceConfig() {
    return sourceConfig;
  }
}

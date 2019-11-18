package edu.stanford.integrator.transform;

import edu.stanford.integrator.config.DataSourceConfig;

public interface TransformWithUserCsvService extends TransformService {
  // User uploads a specific mapping of how two fields relate to each other
  // E.g. in source 0 "Week 0" maps to "W0" in source 1
  DataSourceConfig getDataSourceConfig(DataSourceConfig dataSourceConfig);

  void setDataSourceConfig(DataSourceConfig dataSourceConfig);
}

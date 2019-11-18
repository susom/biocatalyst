package edu.stanford.integrator.transform;

import edu.stanford.integrator.config.DataSourceConfig;

public interface TransformWithInstructionsService extends TransformService {
  // Transform based on an "instructions" object - e.g. important_tokens, delimiter_positions, etc.
  // created from the Configure ID Modal and stored in Elasticsearch currently
  DataSourceConfig getDataSourceConfig(DataSourceConfig dataSourceConfig);

  void setDataSourceConfig(DataSourceConfig dataSourceConfig);

  String transform(String value) throws Exception;
}

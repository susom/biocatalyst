package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.DataSourceConfig;

public interface SampleFieldService {
  void setDataSourceConfig(DataSourceConfig config);

  void setJson(JsonNode jsonNode);

  String sampleOpenspecimen(String field, int counter) throws Exception;

  String sampleRedcap(String field, int counter) throws Exception;

  String sampleCsv(String field, int counter) throws Exception;
}

package edu.stanford.integrator.transform.visitid;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.DataSourceConfig;
import java.util.Iterator;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class VisitIdTransformServiceImpl implements VisitIdTransformService {
  private static final Logger LOGGER = Logger.getLogger(VisitIdTransformService.class.getName());

  public DataSourceConfig dataSourceConfig;

  public DataSourceConfig getDataSourceConfig(DataSourceConfig dataSourceConfig) {
    return this.dataSourceConfig;
  }

  public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
    this.dataSourceConfig = dataSourceConfig;
  }

  public String getVisitIdSource() {
    return this.dataSourceConfig.getVisitIdConnectionsSource();
  }

  public String getVisitIdDestination() {
    return this.dataSourceConfig.getVisitIdConnectionsDestination();
  }

  public JsonNode getVisitIdConnectionsMapping() {
    return this.dataSourceConfig.getVisitIdConnectionsMapping();
  }

  public String transform(String inputValue) {
    DataSourceConfig dataSourceConfig = getDataSourceConfig(this.dataSourceConfig);
    JsonNode visitIdMapping = dataSourceConfig.getVisitIdConnectionsMapping();
    // Iterate through the keys, if there is a match in values, transform
    Iterator<String> fieldNames = visitIdMapping.fieldNames();

    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (inputValue.equals(fieldName)) {
        return visitIdMapping.get(fieldName).textValue();
      }
    }

    return "VISIT_ID_TRANSFORM_NOT_SUPPLIED_IN_MAPPING";
  }

}

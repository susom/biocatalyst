package edu.stanford.biosearch.data.elasticsearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MappingClient {
  private static final Logger logger = Logger.getLogger(MappingClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  // Elastic Mapping
  public Map<String, Object> getMapping(String index) throws IOException {
    String elasticUrl = this.elasticNetwork.elasticsearchUrl.concat("/").concat(index).concat("/")
        .concat("_mapping");

    Map<String, Object> callResult = this.elasticNetwork.get(elasticUrl);
    Object responseCode = callResult.get("responseCode");
    Map<String, Object> responseBody = (Map<String, Object>) callResult.get("responseBody");
    Map<String, Object> indexProperty = (Map<String, Object>) responseBody.get(index);
    Map<String, Object> mappings = (Map<String, Object>) indexProperty.get("mappings");
    Map<String, Object> logs = (Map<String, Object>) mappings.get("doc");
    Map<String, Object> properties = (Map<String, Object>) logs.get("properties");
    logger.info(properties);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(NetworkClient.RESPONSE_BODY, properties);
    result.put(NetworkClient.RESPONSE_CODE, responseCode);
    return result;
  }

  // Get Distinct Column Values
  public Map<String, Object> getDistinctColumnValues(String document, String column) throws IOException {
    String elasticUrl = this.elasticNetwork.elasticsearchUrl.concat("/").concat(document).concat("/_search");
    JSONObject obj = new JSONObject();
    obj.put("size", 0);
    obj.put("aggs", new JSONObject().put("columns",
        new JSONObject().put("terms", new JSONObject().put("field", column + ".raw").put("size", 500))));

    Map<String, Object> callResult = this.elasticNetwork.post(elasticUrl, obj.toString());
    Object responseCode = callResult.get("responseCode");
    Map<String, Object> responseBody = (Map<String, Object>) callResult.get("responseBody");
    Map<String, Object> aggregations = (Map<String, Object>) responseBody.get("aggregations");
    Map<String, Object> columns = (Map<String, Object>) aggregations.get("columns");

    logger.info(columns);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(NetworkClient.RESPONSE_BODY, columns.get("buckets"));
    result.put(NetworkClient.RESPONSE_CODE, responseCode);
    return result;
  }

  public Map<String, Object> getMappings(List<String> indices) throws IOException {
    StringBuilder flatIndex = new StringBuilder();
    for (String index : indices) {
      flatIndex.append(index);
      flatIndex.append(",");
    }
    String elasticUrl = this.elasticNetwork.elasticsearchUrl.concat("/").concat(flatIndex.toString()).concat("/")
        .concat("_mapping");

    Map<String, Object> callResult = this.elasticNetwork.get(elasticUrl);
    Object responseCode = callResult.get("responseCode");
    Map<String, Object> responseBody = (Map<String, Object>) callResult.get("responseBody");
    logger.info(responseBody);
    Map<String, Object> mapping = new HashMap<String, Object>();
    for (Entry<String, Object> index : responseBody.entrySet()) {
      Map<String, Object> properties = this.ExtractProperties(index);
      if (properties == null) {
        continue;
      }
      properties = this.RemoveNonIndexedProperties(properties);
      mapping.put(index.getKey(), properties);
    }

    logger.info(mapping);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(NetworkClient.RESPONSE_BODY, mapping);
    result.put(NetworkClient.RESPONSE_CODE, responseCode);
    return result;
  }

  private Map<String, Object> ExtractProperties(Entry<String, Object> index) {
    Map<String, Object> value = (Map<String, Object>) index.getValue();
    Map<String, Object> mappings = (Map<String, Object>) value.get("mappings");
    Map<String, Object> logs = (Map<String, Object>) mappings.get("doc");
    if (logs == null) {
      return null;
    }
    return (Map<String, Object>) logs.get("properties");
  }

  private Map<String, Object> RemoveNonIndexedProperties(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<String, Object>();
    for (Entry<String, Object> fieldProperties : properties.entrySet()) {
      Map<String, Object> props = (Map<String, Object>) fieldProperties.getValue();
      Boolean indexProperty = (Boolean) props.get("index");
      if (indexProperty == null) {
        result.put(fieldProperties.getKey(), fieldProperties.getValue());
        continue;
      }
      if (indexProperty.booleanValue()) {
        result.put(fieldProperties.getKey(), fieldProperties.getValue());
      }
    }
    return result;
  }

}

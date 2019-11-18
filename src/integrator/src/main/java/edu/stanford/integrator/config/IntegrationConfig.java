package edu.stanford.integrator.config;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class IntegrationConfig {
  private final static Logger LOGGER = Logger.getLogger(IntegrationConfig.class.getName());
  private JsonNode configJSON;

  public IntegrationConfig(JsonNode configJSON) {
    this.configJSON = configJSON;
  }

  public String getName() {
    return this.configJSON.get("_name").asText();
  }

  public String getID() {
    return this.configJSON.get("_id").asText();
  }

  public List<String> getSourceIDs() {
    List<String> sourceIDs = new ArrayList<>();

    Iterator<JsonNode> sourceConfig = configJSON.get("dataSources").elements();
    while (sourceConfig.hasNext()) {

      JsonNode currentDataSourceNode = sourceConfig.next();
      sourceIDs.add(currentDataSourceNode.get("source_id").asText());
    }
    return sourceIDs;
  }

  public DataSourceConfig getSourceConfig(String sourceID) {
    Iterator<JsonNode> sourceConfig = configJSON.get("dataSources").elements();

    while (sourceConfig.hasNext()) {
      JsonNode currentNode = sourceConfig.next();
      if (currentNode.get("source_id").asText().equals(sourceID)) {
        return new DataSourceConfig(currentNode);
      }
    }

    return null;
  }
}

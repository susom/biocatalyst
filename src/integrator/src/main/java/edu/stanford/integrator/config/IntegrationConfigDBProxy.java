package edu.stanford.integrator.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.clients.ElasticsearchClient;
import edu.stanford.integrator.util.Base64Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IntegrationConfigDBProxy {
  private final static Logger LOGGER = Logger.getLogger(IntegrationConfigDBProxy.class.getName());

  @Autowired
  ElasticConfig elasticConfig;

  @Autowired
  Base64Util base64Util;

  private JsonNode integrationsJSON;

  public List<String> getIntegrationIDs() throws Exception {
    loadConfigurations();
    List<String> integrationIDs = new ArrayList<>();
    Iterator<JsonNode> iterator = this.integrationsJSON.elements();
    while (iterator.hasNext()) {
      integrationIDs.add(iterator.next().get("_id").asText());
    }

    return integrationIDs;
  }

  public JsonNode getIntegrationConfig(String integrationID) throws Exception {
    if (integrationID == null || integrationID.equals("")) {
      throw new IllegalArgumentException("Invalid integration ID " + integrationID);
    }

    LOGGER.log(Level.INFO, "Loading configuration from ElasticSearch");
    String integrationJSONStr = null;


    String elasticCredentials = base64Util.encodeCredentials(elasticConfig.user, elasticConfig.pass);
    String elasticBaseUrl = elasticConfig.protocol + "://" + elasticConfig.host + ":" + elasticConfig.port;
    ElasticsearchClient restClient = new ElasticsearchClient(elasticBaseUrl + "/integrations/doc/" + integrationID, elasticCredentials);
    integrationJSONStr = restClient.get();

    if (integrationJSONStr == null || integrationJSONStr.equals("")) {
      throw new Exception("Unable to get the integration configuration from the configurations DB, for:" + integrationID);
    }

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);

    JsonNode configJSON;
    try {
      configJSON = mapper.readTree(integrationJSONStr);
    } catch (IOException ex) {
      throw new Exception("Error while parsing JSON - can't read root node: " + integrationJSONStr + "\n " + ex.getMessage());
    }

    JsonNode integrationJSON = null;
    try {
      integrationJSON = configJSON.get("_source");
    } catch (Exception ex) {
      throw new Exception("No data for integration " + integrationID + ", received: " + configJSON);
    }

    if (integrationJSON == null) {
      LOGGER.log(Level.SEVERE, "Error parsing the integration configuration. \n" + configJSON);
      throw new Exception("Error parsing the integration configuration." + configJSON);
    }

    return integrationJSON;
  }

  private void loadConfigurations() throws Exception {
    LOGGER.log(Level.INFO, "Loading configurations from ElasticSearch");

    String integrationsJSONStr = null;
    int maxResults = 1000000;

    String elasticCredentials = base64Util.encodeCredentials(elasticConfig.user, elasticConfig.pass);
    String elasticBaseUrl = elasticConfig.protocol + "://" + elasticConfig.host + ":" + elasticConfig.port;
    ElasticsearchClient restClient = new ElasticsearchClient(elasticBaseUrl + "/integrations/_search?from=0&size=500?from=0&size=" + maxResults, elasticCredentials);
    integrationsJSONStr = restClient.get();

    if (integrationsJSONStr == null || integrationsJSONStr.equals("")) {
      throw new Exception("No integration configurations");
    }

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);

    JsonNode configJSON;
    try {
      configJSON = mapper.readTree(integrationsJSONStr);
    } catch (IOException ex) {
      throw new Exception("Error while parsing JSON - can't read root node: " + integrationsJSONStr + "\n " + ex.getMessage());
    }

    try {
      integrationsJSON = configJSON.get("hits").get("hits");
    } catch (Exception ex) {
      integrationsJSON = null;
      throw new Exception("No integrations in the integration configurations DB" + configJSON);
    }

    if (integrationsJSON == null) {
      LOGGER.log(Level.SEVERE, "Error parsing the integrations configuration. \n" + configJSON);
      throw new Exception("Error parsing the integrations configuration." + configJSON);
    }
  }
}

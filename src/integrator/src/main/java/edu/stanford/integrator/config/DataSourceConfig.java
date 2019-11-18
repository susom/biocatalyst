package edu.stanford.integrator.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class DataSourceConfig {
  private static final Logger LOGGER = Logger.getLogger(DataSourceConfig.class.getName());

  private final JsonNode configJSON;

  public DataSourceConfig(JsonNode configJSON) {
    if (configJSON == null) {
      throw new IllegalArgumentException("JSON Configuration is Null");
    }
    this.configJSON = configJSON;
  }

  public JsonNode toJSON() {
    return configJSON;
  }

  public String getSourceId() {
    return (configJSON.get("source_id") == null) ? "" : configJSON.get("source_id").asText();
  }

  public String getSourceType() {
    return (configJSON.get("type") == null) ? "" : configJSON.get("type").asText();
  }

  public String getSourceName() {
    return (configJSON.get("name") == null) ? "" : configJSON.get("name").asText();
  }

  public JsonNode getConnectionDetails() {
    return configJSON.get("connection_details");
  }

  public String getCsvStorageID() {
    return getConnectionDetails().get("data").asText();
  }

  public JsonNode getIntegrationDetails() {
    return configJSON.get("integration_details");
  }

  public JsonNode getSubjectCode() {
    return getIntegrationDetails().get("subjectCode");
  }

  public JsonNode getConnections() {
    return getIntegrationDetails().get("connections");
  }

  public JsonNode getSubjectCodeDetails(String type) {
    ArrayNode subjectCodeArray = (ArrayNode) getSubjectCode();
    Iterator<JsonNode> fieldNameIter = subjectCodeArray.iterator();
    while (fieldNameIter.hasNext()) {
      JsonNode item = fieldNameIter.next();
      if (item.get("type").textValue().equals(type)) {
        // return the entire JSON object in the Array of JSON objects
        return item;
      }
    }
    return null;
  }

  public JsonNode getVisitId() {
    return getIntegrationDetails().get("visitId").isNull() ? null : getIntegrationDetails().get("visitId");
  }

  public String getVisitIdFieldFromConnections() {
    // Read the connections for a source, aka how someone wants to connect back to the primary
    // For now this is only one source so .get(0) is valid, but this may not always be the way
    // Feeds into getVisitIdColumn
    return getConnections().get(0).get("visitId").isNull() || getConnections().get(0).get("visitId").get("type").isNull() ? "" : getConnections().get(0).get("visitId").get("type").textValue();
  }

  public String getVisitIdType() {
    return getIntegrationDetails().get("visitId").get(0).get("type").isNull() ? null : getIntegrationDetails().get("visitId").get(0).get("type").textValue();
  }

  public String getVisitIdConnectionsSource() {
    // In a user provided CSV mapping, this is the source that's being configured
    return getConnections().get(0).get("visitId").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").get("source").isNull() ? null : getConnections().get(0).get("visitId").get("eventNameMapping").get("source").textValue();
  }

  public String getVisitIdConnectionsDestination() {
    // In a user provided CSV mapping, this is the source that's being evaluated against, e.g. the primary source
    return getConnections().get(0).get("visitId").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").get("destination").isNull() ? null : getConnections().get(0).get("visitId").get("eventNameMapping").get("destination").textValue();
  }

  public JsonNode getVisitIdConnectionsMapping() {
    // returns the mapping of a between this data source Visit ID info and what it's being compared against.
    // e.g. if a secondary source had "W0" and a primary had "Week 0", the CSV mapping have this info in two columns, and on upload
    // the resulting map would be: { "W0" : "Week 0" }
    return getConnections().get(0).get("visitId").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").isNull() ||
        getConnections().get(0).get("visitId").get("eventNameMapping").get("mapping").isNull() ? null : getConnections().get(0).get("visitId").get("eventNameMapping").get("mapping");
  }

  public String getVisitIdColumn(String type) {
    if (getVisitId().isNull() || type.equals("")) {
      return "";
    }
    ArrayNode visitIdArray = (ArrayNode) getVisitId();
    Iterator<JsonNode> fieldNameIter = visitIdArray.iterator();
    while (fieldNameIter.hasNext()) {
      JsonNode item = fieldNameIter.next();
      if (item.get("type").textValue().equals(type)) {
        return item.get("columnName").textValue();
      }
    }
    return null;
  }

  public String getVisitDateFormat() {
    if (getVisitId().isNull()) {
      return "";
    }
    ArrayNode visitIdArray = (ArrayNode) getVisitId();
    Iterator<JsonNode> fieldNameIter = visitIdArray.iterator();
    while (fieldNameIter.hasNext()) {
      JsonNode item = fieldNameIter.next();
      if (item.get("type").textValue().equals("visitDate")) {
        return item.get("format").textValue();
      }
    }
    return null;
  }

  public List<Integer> getImportantTokens(String type) {
    type = type.toLowerCase();
    List<Integer> tokenListInt = new ArrayList<Integer>();
    ArrayNode arrayNode = (ArrayNode) getSubjectCodeDetails(type).get("important_tokens");
    for (JsonNode item : arrayNode) {
      tokenListInt.add(item.asInt());
    }
    return getSubjectCodeDetails(type).get("important_tokens").isArray() ? tokenListInt : null;
  }

  public Integer[] getImportantTokensOrder(String type) {
    type = type.toLowerCase();
    ArrayNode arrayNode = (ArrayNode) getSubjectCodeDetails(type).get("important_tokens_order");
    Integer[] tokenOrderArr = new Integer[arrayNode.size()];
    Iterator<JsonNode> fieldNameIter = arrayNode.iterator();
    int i = 0;
    while (fieldNameIter.hasNext()) {
      JsonNode item = fieldNameIter.next();
      tokenOrderArr[i] = item.asInt();
      i++;
    }
    return getSubjectCodeDetails(type).get("important_tokens_order").isArray() ? tokenOrderArr : null;
  }

  public List<Integer> getGrowthTokens(String type) {
    type = type.toLowerCase();
    List<Integer> growthTokenListInt = new ArrayList<Integer>();
    ArrayNode arrayNode = (ArrayNode) getSubjectCodeDetails(type).get("growth_tokens");
    for (JsonNode item : arrayNode) {
      growthTokenListInt.add(item.asInt());
    }
    return getSubjectCodeDetails(type).get("growth_tokens").isArray() ? growthTokenListInt : null;
  }

  public List<Integer> getDelimiterPositions(String type) {
    type = type.toLowerCase();
    List<Integer> delimListInt = new ArrayList<Integer>();
    ArrayNode arrayNode = (ArrayNode) getSubjectCodeDetails(type).get("delimiter_positions");
    Iterator<JsonNode> fieldNameIter = arrayNode.iterator();
    while (fieldNameIter.hasNext()) {
      JsonNode item = fieldNameIter.next();
      delimListInt.add(item.asInt());
    }
    return delimListInt;
  }

  public String getMatchingStrategy() {
    if (configJSON.get("matching_strategy") == null) {
      return null;
    }
    return configJSON.get("matching_strategy").asText();
  }

  public boolean isPrimarySource() throws Exception { // all other sources will be integrated with the primary (for now)
    if (getIntegrationDetails() == null) {
      return false;
    }

    if (configJSON.get("primary") == null) {
      throw new Exception("Integration Configuration Data Source is missing the primary flag");
    }

    return configJSON.get("primary").asBoolean();
  }

  public String getConnectionURL() {
    if ((getConnectionDetails() == null) || (getConnectionDetails().get("URL") == null)) {
      return "";
    }
    return getConnectionDetails().get("URL").asText();
  }

  public String getConnectionCredentialsUser() {
    if ((getConnectionDetails() == null) || (getConnectionDetails().get("credentials") == null) || (getConnectionDetails().get("credentials").get("user") == null)) {
      return "";
    }
    return getConnectionDetails().get("credentials").get("user").asText();
  }

  public String getConnectionCredentialsPassword() {
    if ((getConnectionDetails() == null) || (getConnectionDetails().get("credentials") == null) || (getConnectionDetails().get("credentials").get("password") == null)) {
      return "";
    }
    String user = getConnectionDetails().get("credentials").get("user").textValue();
    String password = getConnectionDetails().get("credentials").get("password").textValue();
    if (user != null && !user.isEmpty()) {
      String credentialPlainText = user + ":" + password;
      String credentialEncoded = Base64.getEncoder().encodeToString(credentialPlainText.getBytes());
      String credential = "Basic " + credentialEncoded;
      return credential;
    } else {
      return password;
    }
  }

  public String getData() {
    if ((getConnectionDetails() == null) || (getConnectionDetails().get("data") == null)) {
      return "";
    }
    return getConnectionDetails().get("data").asText();
  }

  public String getIdColumnNamePpid() {
    if (getSubjectCodeDetails("ppid") != null) {
      if (getSubjectCodeDetails("ppid").get("columnName") != null) {
        return getSubjectCodeDetails("ppid").get("columnName").textValue();
      }
    }
    return null;
  }

  public String getSqlAlias() {
    // e.g. S0 for primary, S1 for the first imported table
    return "S" + getSourceId();
  }

  public String getIdColumnNameMrn() {
    if (getSubjectCodeDetails("mrn") != null) {
      if (getSubjectCodeDetails("mrn").get("columnName") != null) {
        return getSubjectCodeDetails("mrn").get("columnName").textValue();
      }
    }
    return null;
  }

  public String getConnectToPrimaryIdType() {
    // only for non-primary sources
    return (getSourceId().equals("0")) ? "" : getIntegrationDetails().get("connections").get(0).get("subjectCodeType").asText();
  }

  public boolean isIntegratableByEvent() {
    // Default = false
    if ((getIntegrationDetails() == null) || (getIntegrationDetails().get("integratableByEvent") == null)) {
      return false;
    }
    return getIntegrationDetails().get("integratableByEvent").asBoolean();
  }

  public JsonNode getEventNameMapping() {
    if ((getIntegrationDetails() == null) || (getIntegrationDetails().get("eventNameMapping") == null)) {
      return null;
    }
    return getIntegrationDetails().get("eventNameMapping");
  }

  public boolean isIntegratableByDate() {
    if ((getIntegrationDetails() == null) || (getIntegrationDetails().get("integratableByDate") == null)) {
      return false;
    }
    return getIntegrationDetails().get("integratableByDate").asBoolean();
  }

  public String getDateColumnName() {
    if ((getIntegrationDetails() == null) || (getIntegrationDetails().get("dateColumnName") == null)) {
      return "";
    }
    return getIntegrationDetails().get("dateColumnName").asText();
  }

  public int getDateMatchingRangeInDays() {
    // return 0 by default
    return getConnections().get(0).get("visitId").get("dateMatchingRangeInDays").isNull() ? 0 : getConnections().get(0).get("visitId").get("dateMatchingRangeInDays").asInt();
  }

  public String getEventNameColumn() {
    if ((getIntegrationDetails() == null) || (getIntegrationDetails().get("eventNameColumn") == null)) {
      return "";
    }
    return getIntegrationDetails().get("eventNameColumn").asText();
  }

  @Override
  public String toString() {
    try {
      return (new ObjectMapper()).writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(this.toJSON());
    } catch (JsonProcessingException ex) {
      return toJSON().toString();
    }
  }

  public String getProcessedId(String type) {

    return getSubjectCodeDetails(type).get("processed_id").isNull() ? null : getSubjectCodeDetails(type).get("processed_id").textValue();
  }
}
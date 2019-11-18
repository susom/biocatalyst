package edu.stanford.integrator.importer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.integrator.clients.OpenSpecimenClient;
import edu.stanford.integrator.config.DataSourceConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OPENSPECIMENImporter extends AbstractImporter {
  private static final Logger LOGGER = Logger.getLogger(OPENSPECIMENImporter.class.getName());

  @Value("getAllSpecimenQuery")
  String getAllSpecimenQuery;

  @Value("getOneSpecimenQuery")
  String getOneSpecimenQuery;

  @Autowired
  ImporterUtil importerUtil;

  // Importers exist in the moment and are deleted, so there should be no worry about caching an mrn list as after the integration is complete this importer release.
  // May need to explicitly delete importers from now on to reduce time mrn list is available in memory. (<--- Super Hackers)

  @Override
  protected Set<String> importDataImpl(String importTableName, DataSourceConfig config, String remoteUser, Set<String> MRNList) throws Exception {
    String importedData = getSpecimenData(config);

    // formatting the imported data before sending it to the datalake
    JsonNode nodeWithTableRows = getTableRowsFromJSON(importedData);
    if (nodeWithTableRows == null) {
      throw new Exception("Unable to import report data, no  records");
    }

    JsonNode dataToImport = nodeWithTableRows;
    this.mrnList = importerUtil.ExtractUniqueColumnDataFromJsonNodeArray(dataToImport, "MRN"); // <-- Hard coding becuase it's open specimen for now.
    // Remove Null Values
    this.mrnList.remove("null");
    this.mrnList.remove(null);
    try {
      importerUtil.insertData(importTableName, dataToImport, config);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error while importing Specimen data. " + ex.getLocalizedMessage());
      throw new Exception("Error while importing Specimen data. " + ex.getLocalizedMessage());
    }

    // Returning List of created tables
    Set<String> createdTables = new LinkedHashSet<String>();
    createdTables.add(importTableName);
    return createdTables;
  }

  public String getSpecimenData(DataSourceConfig sourceConfig) throws Exception {

    if (sourceConfig == null) {
      throw new IllegalArgumentException("The specimen import configuration is missing");
    }

    String DBURL = sourceConfig.getConnectionURL();
    if (DBURL == null || DBURL.equals("")) {
      throw new IllegalArgumentException("The specimen source URL is missing");
    }

    String credentials = sourceConfig.getConnectionCredentialsPassword();
    if (credentials == null || credentials.equals("")) {
      throw new IllegalArgumentException("The credentials for connecting to the specimen source are missing");
    }

    String collectionProtocolID = sourceConfig.getData();
    if (collectionProtocolID == null) {
      throw new Exception("Missing OpenSpecimen integration information: OpenSpecimen CPID");
    }

    String queryString = getAllSpecimenQuery;
    queryString = queryString.replace("CollectionProtocolID", collectionProtocolID);
    LOGGER.log(Level.INFO, "Get All Specimen Query: " + queryString);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode JSON = mapper.readTree(queryString);
    OpenSpecimenClient client = new OpenSpecimenClient(DBURL, credentials);
    CloseableHttpResponse response = client.post(JSON);

    String responseStr = convertToJSONString(response, " Specimen for CP" + collectionProtocolID);

    String curatedResponse = curateResponse(responseStr);

    return curatedResponse;
  }

  public String getOneSpecimenData(DataSourceConfig sourceConfig, int counter) throws Exception {
    //TODO: refactor this to be more DRY
    if (sourceConfig == null) {
      throw new IllegalArgumentException("The specimen import configuration is missing");
    }

    String DBURL = sourceConfig.getConnectionURL();
    if (DBURL == null || DBURL.equals("")) {
      throw new IllegalArgumentException("The specimen source URL is missing");
    }

    String credentials = sourceConfig.getConnectionCredentialsPassword();
    if (credentials == null || credentials.equals("")) {
      throw new IllegalArgumentException("The credentials for connecting to the specimen source are missing");
    }

    String collectionProtocolID = sourceConfig.getData();
    if (collectionProtocolID == null) {
      throw new Exception("Missing OpenSpecimen integration information: OpenSpecimen CPID");
    }

    String queryString = getOneSpecimenQuery;
    queryString = queryString.replace("CollectionProtocolID", collectionProtocolID);
    LOGGER.log(Level.INFO, "Get One Specimen Query: " + queryString);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode JSON = mapper.readTree(queryString);
    OpenSpecimenClient client = new OpenSpecimenClient(DBURL, credentials);
    CloseableHttpResponse response = client.post(JSON);

    String responseStr = convertToJSONString(response, " Specimen for CP" + collectionProtocolID);

    String curatedResponse = curateResponse(responseStr);

    return curatedResponse;
  }

  protected String convertToJSONString(HttpResponse resp, String jsonObjectName) throws Exception {

    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Unable to read HTTP response content", ex);
      throw new Exception("Unable to read HTTP response content");
    }

    StringBuilder result = new StringBuilder();
    result.append("{ \"").append(jsonObjectName).append("\": ");

    String line = null;
    while ((line = reader.readLine()) != null) {
      result.append(line);
    }

    result.append("}");

    return result.toString();
  }

  private JsonNode getTableRowsFromJSON(String json) throws Exception {

    if (json == null || json.equals("")) {
      throw new IllegalArgumentException("getTableRowsFromJSON: json string is null or empty");
    }

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    JsonNode rootNode = null;

    try {
      rootNode = mapper.readTree(json);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Error while parsing JSON - can't read root node", ex);
      throw new Exception("Error while parsing JSON - can't read root node: " + json + ex.getMessage());
    }

    // expecting a JSON where the root node contains the CP name as the first field, which intern contains 3 fields:  columnLabels, columnUrls, rows
    // each of those is of type array. the first two are string arrays. the  rows array contains elements - each one is a string arrays of values.
    // we need to conver this to a JSON that contains maps (each row is one map), of <column name, value>
    JsonNode topNode = rootNode.get(rootNode.fieldNames().next());

    ArrayNode columnNamesArray = (ArrayNode) topNode.get("columnLabels");
    ArrayNode rowsArray = (ArrayNode) topNode.get("rows");

    Entry<String, JsonNode> tableRows = convertToMappedRows(rowsArray, columnNamesArray).fields().next();
    if (tableRows == null) {
      throw new Exception("Error while parsing JSON -  root node first field is NULL");
    }

    return tableRows.getValue();
  }

  private JsonNode convertToMappedRows(ArrayNode rowsArray, ArrayNode columnNamesArray) {
    int numberOfColumns = columnNamesArray.size();

    ArrayNode rowsNode = new ArrayNode(JsonNodeFactory.instance);

    Iterator<JsonNode> iterator = rowsArray.elements();
    while (iterator.hasNext()) {
      ArrayNode currentRowNode = (ArrayNode) iterator.next();
      ObjectNode newRowNode = JsonNodeFactory.instance.objectNode();
      for (int i = 0; i < numberOfColumns; ++i) {
        newRowNode.put(columnNamesArray.get(i).asText(), currentRowNode.get(i).asText());
      }

      rowsNode.add(newRowNode);
    }

    ObjectNode rows = JsonNodeFactory.instance.objectNode();
    rows.put("rows", rowsNode);
    return rows;
  }

  public String curateResponse(String responseStr) {
    StringBuffer curated = new StringBuffer(responseStr);

    String substring = "Specimen# ";
    String replacement = "Specimen ";
    int position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Specimen Specimen";
    replacement = "Specimen";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Participant# ";
    replacement = "Participant ";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Participant Particpiant";
    replacement = "Participant";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Visit# Visit";
    replacement = "Visit";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Visit# ";
    replacement = "Visit ";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Collection Protocol# ";
    replacement = "Collection Protocol ";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "Collection Protocol Collection Protocol";
    replacement = "Collection Protocol";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    substring = "$";
    replacement = "";
    position = curated.lastIndexOf(substring);
    if (position >= 0) {
      curated.replace(position, position + substring.length(), replacement);
    }

    return curated.toString();
  }
}

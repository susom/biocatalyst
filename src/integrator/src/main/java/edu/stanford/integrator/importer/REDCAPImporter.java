package edu.stanford.integrator.importer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.stanford.integrator.clients.REDCAPClient;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.importer.redcap.RedcapColumnCondensorMapping;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class REDCAPImporter extends AbstractImporter {

  private static final Logger LOGGER = Logger.getLogger(REDCAPImporter.class.getName());

  @Autowired
  ImporterUtil importerUtil;

  @Value("${redcap.api.shared.token}")
  String sharedKey;

  @Override
  protected Set<String> importDataImpl(String importTableName, DataSourceConfig config, String remoteUser,
                                       Set<String> MRNList) throws Exception {

    String reportJSON = getReportJSON(config, remoteUser);
    if (reportJSON == null) {
      throw new Exception("Can't process the report - the report JSON is null");
    }

    JsonNode nodeWithTableRows = getTableRowsFromJSON(reportJSON);
    if (nodeWithTableRows == null) {
      throw new Exception("Unable to import report data, no  records");
    }

    JsonNode dataToImport = nodeWithTableRows;

    // We only need to do this if we're a primary source with the MRN field set
    // or a secondary source connecting back via MRN
    if ((config.getSourceId().equals("0") && config.getIdColumnNameMrn() != null) ||
        config.getConnectToPrimaryIdType().equalsIgnoreCase("mrn")) {
      this.mrnList = importerUtil.ExtractUniqueColumnDataFromJsonNodeArray(dataToImport,
          config.getIdColumnNameMrn());
    }

    // Request Headers for this pro
    String headerJSON = getColumnJSON(config, remoteUser);
    ArrayNode nodeColumnRows = getColumnRowsFromJSON(headerJSON);
    if (nodeColumnRows == null) {
      throw new Exception("Unable to import report data, no  records");
    }

    RedcapColumnCondensorMapping columnCondensorMapping = new RedcapColumnCondensorMapping();
    columnCondensorMapping.generateColumnMapping(nodeColumnRows);
    JsonNode condensedDataToImport = columnCondensorMapping.condenseCheckboxColumnsInData((ArrayNode) dataToImport);
    try {
      importerUtil.insertData(importTableName, condensedDataToImport, config);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Cannot insert data: ", ex);
    }

    // Returning List of created tables
    Set<String> createdTables = new LinkedHashSet<String>();
    createdTables.add(importTableName);
    return createdTables;
  }

  private JsonNode getTableRowsFromJSON(String json) throws Exception {
    JsonNode rootNode = parseJson(json);

    // expecting a JSON where the root node contains the report name as the first
    // field, and the next field contains the report rows
    Entry<String, JsonNode> tableRows = rootNode.fields().next();
    if (tableRows == null) {
      throw new Exception("Error while parsing JSON -  root node first field is NULL");
    }

    return tableRows.getValue();
  }

  private ArrayNode getColumnRowsFromJSON(String json) throws Exception {
    JsonNode rootNode = parseJson(json);

    // expecting a JSON where the root node contains the report name as the first
    // field,
    // and the next field contains the report rows
    Entry<String, JsonNode> tableRows = rootNode.fields().next();
    JsonNode root = tableRows.getValue();
    ArrayNode columns = (ArrayNode) root.get("columns");
    if (columns == null) {
      throw new Exception("Error while parsing JSON -  root node first field is NULL");
    }

    return columns;
  }

  public String getReportJSON(DataSourceConfig config, String remoteUser) throws Exception {
    return new REDCAPClient().getReportJSON(config.getConnectionURL(), config.getConnectionCredentialsPassword(),
        config.getData(), remoteUser, sharedKey);
  }

  public String getColumnJSON(DataSourceConfig config, String remoteUser) throws Exception {
    return new REDCAPClient().getColumnJSON(config.getConnectionURL(), config.getConnectionCredentialsPassword(),
        config.getData(), remoteUser, sharedKey);
  }

  public JsonNode parseJson(String json) throws Exception {

    if (json == null || json.equals("")) {
      throw new IllegalArgumentException();
    }

    // LOGGER.log(Level.INFO, "Parsing JSON... ");

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    JsonNode rootNode = null;

    if (mapper == null) {
      throw new Exception("Error while parsing JSON - mapper is null");
    }

    try {
      rootNode = mapper.readTree(json);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Error while parsing JSON - can't read root node", ex);
      throw new Exception(
          "Error while parsing JSON - can't read root node: " + json + ex.getMessage());
    }

    return rootNode;
  }

}

package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.importer.NoMoreDataException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class SampleFieldServiceImpl implements SampleFieldService {
  private static final Logger LOGGER = Logger.getLogger(SampleFieldServiceImpl.class.getName());

  private DataSourceConfig config;
  private JsonNode jsonNode;

  public DataSourceConfig getDataSourceConfig() {
    return this.config;
  }

  public JsonNode getJson() {
    return this.jsonNode;
  }

  public void setDataSourceConfig(DataSourceConfig config) {
    this.config = config;
  }

  public void setJson(JsonNode jsonNode) {
    this.jsonNode = jsonNode;
  }

  public String sampleOpenspecimen(String field, int counter) throws Exception {
    field = field.toLowerCase();
    String columnName = getColumnName(field);

    // walk through JSON and find the index of columnLabel and use it to look up
    // corresponding row for OpenSpecimen
    Iterator<String> it = this.jsonNode.fieldNames();
    String studyName = it.next();

    JsonNode columnLabels = this.jsonNode.get(studyName).get("columnLabels");

    // Loops through the columnLabels and get the index # to lookup in the Rows json object
    Integer ctr = 0;
    Integer index = null;
    try {
      for (Iterator<JsonNode> itLabels = columnLabels.elements(); itLabels.hasNext(); ) {
        if (columnName.equals(itLabels.next().textValue())) {
          index = ctr;
        }
        ctr++;
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Failed to walk through JSON object ", ex);
      throw new Exception("Failed to walk through JSON object");
    }

    // Look up the ID based on the index position for either PPID or MRN
    try {
      String singleString = this.jsonNode.get(studyName).get("rows").get(counter).get(index).textValue().replace("\"", "");
      return singleString;
    } catch (Exception ex) {
      throw new NoMoreDataException("No more data left to iterate through for 'shuffle the deck' algorithm. Shuffled through: " + counter + " records");
    }
  }

  public String sampleRedcap(String field, int counter) throws Exception {
    field = field.toLowerCase();
    String columnName = getColumnName(field); // the actual field name that is in the source configuration in ES
    Iterator<String> it = this.jsonNode.fieldNames();

    // Just pull off one field, no need to iterate
    String sampledField = it.next();

    try {
      String singleString = this.jsonNode.get(sampledField).get(counter).get(columnName).textValue().replace("\"", "");
      return singleString;
    } catch (Exception ex) {
      throw new NoMoreDataException("No more data left to iterate through for 'shuffle the deck' algorithm. Shuffled through: " + counter + " records");
    }
  }

  public String sampleCsv(String field, int counter) throws Exception {
    field = field.toLowerCase();
    String columnName = getColumnName(field);

    try {
      // Should be an Array Node with CSVs
      LOGGER.log(Level.INFO, "Successfully got CSV data!");
      String singleString = this.jsonNode.get(counter).get(columnName).textValue().replace("\"", "");

      return singleString;
    } catch (Exception ex) {
      throw new NoMoreDataException("No more data left to iterate through for 'shuffle the deck' algorithm. Shuffled through: " + counter + " records");
    }
  }

  private String getColumnName(String field) {
    field = field.toLowerCase();
    String columnName = ""; // the actual field name that is in the source configuration in ES

    switch (field) {
      case "ppid":
        // We only configure PPIDs, never MRNs (as of now)
        columnName = this.config.getIdColumnNamePpid();
        break;
      case "visitdate":
        columnName = this.config.getVisitIdColumn("visitDate");
        break;
    }

    LOGGER.log(Level.INFO, "Sampling field: " + columnName);
    return columnName;
  }
}

package edu.stanford.integrator.importer.redcap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RedcapColumnCondensorMapping {

  private static ObjectMapper mapper = new ObjectMapper();
  private Map<String, RedcapApiColumn> mapping = new HashMap<String, RedcapApiColumn>();

  public RedcapColumnCondensorMapping() {
  }

  // Takes Redcap Columns and creates a ColumnMapping object used
  // in CondenseCheckboxColumnsInData to condense checkbox data into a single property
  public void generateColumnMapping(ArrayNode Columns) {
    mapping = new HashMap<String, RedcapApiColumn>();
    for (JsonNode column : Columns) {

      // Convert
      RedcapApiColumn apiColumn = mapper.convertValue(column, RedcapApiColumn.class);
      List<String> fieldOptions = apiColumn.FieldOptions();

      List<String> optionsIDs = new ArrayList<String>();
      for (String option : fieldOptions) {
        String[] tokens = option.split(",");
        if (tokens.length >= 0) {
          optionsIDs.add(tokens[0]);
        } else {
          // Running into unexpected results. This means that we were not
          // able to parse a particular column for some reason
          tokens = option.split(",");
        }
      }

      for (String id : optionsIDs) {
        String predicted_checkbox_field = String.format("%s___%s", apiColumn.getField_name(), id);
        mapping.put(predicted_checkbox_field, apiColumn);
      }
    }
  }

  public ArrayNode condenseCheckboxColumnsInData(ArrayNode Data) {
    ArrayNode condensed = mapper.createArrayNode();
    for (JsonNode row : Data) {
      JsonNode result = CondenseCheckboxColumn(row);
      condensed.add(result);
    }
    return condensed;
  }

  public JsonNode CondenseCheckboxColumn(JsonNode row) {
    JsonNode condensedRow = mapper.createObjectNode();
    // Iterate through properties
    Iterator<Map.Entry<String, JsonNode>> headers = row.fields();
    while (headers.hasNext()) {
      Map.Entry<String, JsonNode> currentHeader = headers.next();
      String currentHeaderField = currentHeader.getKey();
      String currentHeaderValue = currentHeader.getValue().asText();
      ObjectNode condensedRowPtr = ((ObjectNode) condensedRow);

      // Check if Mapping exists (It should)
      if (mapping.containsKey(currentHeaderField)) {

        // Check if it's a Checkbox

        RedcapApiColumn mappingColumn = mapping.get(currentHeaderField);
        if (mappingColumn.getField_type().equals("checkbox")) {
          // Create new field name if it doesn't exist.
          String mappingColumnName = mappingColumn.getField_name();
          if (!condensedRowPtr.has(mappingColumnName)) {
            condensedRowPtr.putNull(mappingColumnName);
          }

          // If this field is checked
          if (currentHeaderValue.equals("Checked")) {

            // Find Checked Value in mapping Columns
            // Generate options from this mappings redcapapicolumn object
            List<String> fieldOptions = mappingColumn.FieldOptions();

            for (String option : fieldOptions) {
              // tokenize options based on ,
              String[] tokens = option.split(",");

              // Extract choice from datafield
              String[] choiceTokens = currentHeaderField
                  .split(mappingColumn.getField_name().concat("___"));

              if (tokens[0].equals(choiceTokens[1])) {
                // append [1] token to this data nodes base name.
                JsonNode value = condensedRow.get(mappingColumnName);
                String text = value.asText();
                if (value == NullNode.getInstance()) {
                  text = "";
                }
                if (text.length() == 0) {
                  text = text.concat(tokens[1]);
                } else {
                  text = text.concat(",").concat(tokens[1]);
                }

                condensedRowPtr.put(mappingColumnName, text.trim());
              }
            }

          }

        } else {
          condensedRowPtr.put(currentHeaderField, currentHeaderValue);
        }
      } else {
        condensedRowPtr.put(currentHeaderField, currentHeaderValue);
      }
    }
    return condensedRow;
  }
}

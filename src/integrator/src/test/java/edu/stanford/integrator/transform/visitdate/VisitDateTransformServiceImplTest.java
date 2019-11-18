package edu.stanford.integrator.transform.visitdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.config.DataSourceConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitDateTransformServiceImplTest {
  private String dateDataSourceConfig = "    {\n" +
      "      \"connection_details\": {\n" +
      "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
      "        \"credentials\": {\n" +
      "          \"password\": \"passwd\",\n" +
      "          \"user\": \"admin@admin.com\"\n" +
      "        },\n" +
      "        \"data\": 1140,\n" +
      "        \"filename\": null,\n" +
      "        \"headers\": [\n" +
      "          \"Subject Id\",\n" +
      "          \"Visit ID\",\n" +
      "          \"Gender\"\n" +
      "        ]\n" +
      "      },\n" +
      "      \"deletable\": false,\n" +
      "      \"integration_details\": {\n" +
      "        \"connections\": [\n" +
      "          {\n" +
      "            \"matching_strategy\": null,\n" +
      "            \"sourceId\": null,\n" +
      "            \"subjectCodeType\": null,\n" +
      "            \"visitId\": null\n" +
      "          }\n" +
      "        ],\n" +
      "        \"subjectCode\": [\n" +
      "          {\n" +
      "            \"columnLabel\": \"Participant ID Column\",\n" +
      "            \"columnName\": \"PPID\",\n" +
      "            \"configurableColumn\": true,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": \"\",\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"ppid\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Participant MRN Column\",\n" +
      "            \"columnName\": \"MRN\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"mrn\"\n" +
      "          }\n" +
      "        ],\n" +
      "        \"visitId\": [\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Date Column\",\n" +
      "            \"columnName\": \"Specimen_Collection_Date\",\n" +
      "            \"configurableColumn\": true,\n" +
      "            \"configurableType\": true,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"format\": \"yyyy-MM-dd\",\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitDate\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Code Column\",\n" +
      "            \"columnName\": \"EVENT_NAME\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitCode\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Label Column\",\n" +
      "            \"columnName\": \"Visit# Event Label\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitLabel\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Other Visit Column\",\n" +
      "            \"columnName\": null,\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"other\"\n" +
      "          }\n" +
      "        ]\n" +
      "      },\n" +
      "      \"matching_strategy\": null,\n" +
      "      \"name\": \"Specimen Inventory Data\",\n" +
      "      \"primary\": true,\n" +
      "      \"processed_id\": null,\n" +
      "      \"source_id\": \"0\",\n" +
      "      \"type\": \"OPENSPECIMEN\",\n" +
      "      \"valid\": true\n" +
      "    }";

  private ObjectMapper dateMapper = new ObjectMapper();
  private JsonNode dateConfigJson = dateMapper.readTree(dateDataSourceConfig);
  private DataSourceConfig dateConfig = new DataSourceConfig(dateConfigJson);

  private String dateTimeDataSourceConfig = "    {\n" +
      "      \"connection_details\": {\n" +
      "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
      "        \"credentials\": {\n" +
      "          \"password\": \"passwd\",\n" +
      "          \"user\": \"admin@admin.com\"\n" +
      "        },\n" +
      "        \"data\": 1140,\n" +
      "        \"filename\": null,\n" +
      "        \"headers\": [\n" +
      "          \"Subject Id\",\n" +
      "          \"Visit ID\",\n" +
      "          \"Gender\"\n" +
      "        ]\n" +
      "      },\n" +
      "      \"deletable\": false,\n" +
      "      \"integration_details\": {\n" +
      "        \"connections\": [\n" +
      "          {\n" +
      "            \"matching_strategy\": null,\n" +
      "            \"sourceId\": null,\n" +
      "            \"subjectCodeType\": null,\n" +
      "            \"visitId\": null\n" +
      "          }\n" +
      "        ],\n" +
      "        \"subjectCode\": [\n" +
      "          {\n" +
      "            \"columnLabel\": \"Participant ID Column\",\n" +
      "            \"columnName\": \"PPID\",\n" +
      "            \"configurableColumn\": true,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": \"\",\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"ppid\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Participant MRN Column\",\n" +
      "            \"columnName\": \"MRN\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"mrn\"\n" +
      "          }\n" +
      "        ],\n" +
      "        \"visitId\": [\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Date Column\",\n" +
      "            \"columnName\": \"Specimen_Collection_Date\",\n" +
      "            \"configurableColumn\": true,\n" +
      "            \"configurableType\": true,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"format\": \"MM-dd-yyyy HH:mm\",\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitDate\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Code Column\",\n" +
      "            \"columnName\": \"EVENT_NAME\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitCode\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Visit Label Column\",\n" +
      "            \"columnName\": \"Visit# Event Label\",\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"visitLabel\"\n" +
      "          },\n" +
      "          {\n" +
      "            \"columnLabel\": \"Other Visit Column\",\n" +
      "            \"columnName\": null,\n" +
      "            \"configurableColumn\": false,\n" +
      "            \"configurableType\": false,\n" +
      "            \"delimiter_positions\": [],\n" +
      "            \"growth_tokens\": [],\n" +
      "            \"important_tokens\": [],\n" +
      "            \"important_tokens_order\": [],\n" +
      "            \"processed_id\": null,\n" +
      "            \"removeable\": false,\n" +
      "            \"type\": \"other\"\n" +
      "          }\n" +
      "        ]\n" +
      "      },\n" +
      "      \"matching_strategy\": null,\n" +
      "      \"name\": \"Specimen Inventory Data\",\n" +
      "      \"primary\": true,\n" +
      "      \"processed_id\": null,\n" +
      "      \"source_id\": \"0\",\n" +
      "      \"type\": \"OPENSPECIMEN\",\n" +
      "      \"valid\": true\n" +
      "    }";

  private ObjectMapper dateTimeMapper = new ObjectMapper();
  private JsonNode dateTimeConfigJson = dateTimeMapper.readTree(dateTimeDataSourceConfig);
  private DataSourceConfig dateTimeConfig = new DataSourceConfig(dateTimeConfigJson);

  private VisitDateTransformService vdts = new VisitDateTransformServiceImpl();

  VisitDateTransformServiceImplTest() throws IOException {
  }

  @Test
  void transformDate() throws Exception {
    vdts.setDataSourceConfig(dateConfig);
    String transformedId = vdts.transform("1985-05-25");
    assertEquals("1985-05-25 00:00:00", transformedId);
  }

  @Test
  void transformDateTime() throws Exception {
    vdts.setDataSourceConfig(dateTimeConfig);
    String transformedId = vdts.transform("05-25-1985 17:17");
    assertEquals("1985-05-25 17:17:00", transformedId);
  }

}
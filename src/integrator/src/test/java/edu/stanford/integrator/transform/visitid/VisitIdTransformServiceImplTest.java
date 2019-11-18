package edu.stanford.integrator.transform.visitid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.config.DataSourceConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VisitIdTransformServiceImplTest {
  private String exampleDataSourceConfig = "{\n" +
      "                \"source_id\": \"1\",\n" +
      "                \"name\": \"New Data Source\",\n" +
      "                \"type\": \"REDCAP\",\n" +
      "                \"connection_details\": {\n" +
      "                    \"URL\": \"\",\n" +
      "                    \"filename\": null,\n" +
      "                    \"headers\": null,\n" +
      "                    \"data\": \"14649\",\n" +
      "                    \"credentials\": {\n" +
      "                        \"user\": \"\",\n" +
      "                        \"password\": \"45260\"\n" +
      "                    }\n" +
      "                },\n" +
      "                \"integration_details\": {\n" +
      "                    \"subjectCode\": [\n" +
      "                        {\n" +
      "                            \"type\": \"ppid\",\n" +
      "                            \"columnLabel\": \"Participant ID Column\",\n" +
      "                            \"columnName\": \"subject_id\",\n" +
      "                            \"delimiter_positions\": [\n" +
      "                                \"2\"\n" +
      "                            ],\n" +
      "                            \"important_tokens\": [\n" +
      "                                \"0\",\n" +
      "                                \"1\"\n" +
      "                            ],\n" +
      "                            \"important_tokens_order\": [],\n" +
      "                            \"growth_tokens\": [\n" +
      "                                \"1\"\n" +
      "                            ],\n" +
      "                            \"processed_id\": \"15-140\",\n" +
      "                            \"configurableColumn\": true,\n" +
      "                            \"configurableType\": true,\n" +
      "                            \"removeable\": false\n" +
      "                        }\n" +
      "                    ],\n" +
      "                    \"visitId\": [\n" +
      "                        {\n" +
      "                            \"type\": \"visitMapping\",\n" +
      "                            \"columnLabel\": \"Specimen Visit Date\",\n" +
      "                            \"columnName\": \"visit_date\",\n" +
      "                            \"delimiter_positions\": [],\n" +
      "                            \"important_tokens\": [],\n" +
      "                            \"important_tokens_order\": [],\n" +
      "                            \"growth_tokens\": [],\n" +
      "                            \"processed_id\": null,\n" +
      "                            \"configurableColumn\": false,\n" +
      "                            \"configurableType\": true,\n" +
      "                            \"removeable\": true\n" +
      "                        }\n" +
      "                    ],\n" +
      "                    \"connections\": [\n" +
      "                        {\n" +
      "                            \"sourceId\": \"0\",\n" +
      "                            \"subjectCodeType\": \"ppid\",\n" +
      "                            \"visitId\": {\n" +
      "                                \"type\": \"visitMapping\",\n" +
      "                                \"eventNameMapping\": {\n" +
      "                                    \"source\": \"test_source\",\n" +
      "                                    \"destination\": \"test_destination\",\n" +
      "                                    \"mapping\": {\n" +
      "                                        \"Screen/Week 0\": \"W0\",\n" +
      "                                        \"Week 12\": \"W12\",\n" +
      "                                        \"Week 24\": \"W24\",\n" +
      "                                        \"Week 36\": \"W36\"\n" +
      "                                    }\n" +
      "                                },\n" +
      "                                \"dateMatchingRangeInDays\": 0\n" +
      "                            },\n" +
      "                            \"matching_strategy\": null\n" +
      "                        }\n" +
      "                    ]\n" +
      "                },\n" +
      "                \"matching_strategy\": null,\n" +
      "                \"processed_id\": null,\n" +
      "                \"deletable\": true,\n" +
      "                \"valid\": true,\n" +
      "                \"primary\": false\n" +
      "            }";

  private ObjectMapper mapper = new ObjectMapper();
  private JsonNode configJson = mapper.readTree(exampleDataSourceConfig);
  private DataSourceConfig testConfig = new DataSourceConfig(configJson);

  private VisitIdTransformService vits = new VisitIdTransformServiceImpl();

  public VisitIdTransformServiceImplTest() throws IOException {
  }

  @Test
  void processedIdGettersReturnsCorrectValues() {
    vits.setDataSourceConfig(testConfig);
    // Getting processed_id from the DatasourceConfig (model stored in elasticsearch)
    assertEquals("test_source", vits.getVisitIdSource());
    assertEquals("test_destination", vits.getVisitIdDestination());

    String firstValue = vits.getVisitIdConnectionsMapping().get("Screen/Week 0").asText();
    assertEquals("W0", firstValue);

  }

  @Test
  void transformReturnsCorrectFirstValue() throws Exception {
    vits.setDataSourceConfig(testConfig);
    String transformedId = vits.transform("Screen/Week 0");
    assertEquals("W0", transformedId);
  }

  @Test
  void transformReturnsCorrectLastValue() throws Exception {
    vits.setDataSourceConfig(testConfig);
    String transformedId = vits.transform("Week 36");
    assertEquals("W36", transformedId);
  }
}

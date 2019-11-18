package edu.stanford.integrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.services.TokenGrowthServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import edu.stanford.integrator.config.DataSourceConfig;

import java.util.ArrayList;
import java.util.List;


public class TokenGrowthServiceImplTest  {

    private String lastPositionGrowthString = "{\n" +
            "      \"connection_details\": {\n" +
            "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
            "        \"credentials\": {\n" +
            "          \"password\": \"test\",\n" +
            "          \"user\": \"test@test.com\"\n" +
            "        },\n" +
            "        \"data\": 1140,\n" +
            "        \"filename\": null,\n" +
            "        \"headers\": null\n" +
            "      },\n" +
            "      \"deletable\": false,\n" +
            "      \"integration_details\": {\n" +
            "        \"connections\": [],\n" +
            "        \"subjectCode\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant ID Column Name\",\n" +
            "            \"columnName\": \"PPID\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [\"7\"],\n" +
            "            \"important_tokens\": [\n" +
            "              \"0\",\n" +
            "              \"1\"\n" +
            "            ],\n" +
            "            \"important_tokens_order\": [\n" +
            "              \"0\",\n" +
            "              \"1\"\n" +
            "            ],\n" +
            "            \"processed_id\": \"PRIMARY-123\",\n" +
            "            \"type\": \"ppid\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant MRN Column Name\",\n" +
            "            \"columnName\": \"MRN\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"mrn\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"visitId\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Date Column\",\n" +
            "            \"columnName\": \"Visit# Visit Date\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitDate\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Label Column\",\n" +
            "            \"columnName\": \"Visit# Event Label\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitLabel\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Event Column\",\n" +
            "            \"columnName\": \"EVENT_NAME\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitCode\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matching_strategy\": null,\n" +
            "      \"name\": \"Specimen Data\",\n" +
            "      \"primary\": true,\n" +
            "      \"processed_id\": null,\n" +
            "      \"source_id\": \"0\",\n" +
            "      \"type\": \"OPENSPECIMEN\",\n" +
            "      \"valid\": true\n" +
            "    }";

    private String middlePositionGrowthString = "{\n" +
            "      \"connection_details\": {\n" +
            "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
            "        \"credentials\": {\n" +
            "          \"password\": \"test\",\n" +
            "          \"user\": \"test@test.com\"\n" +
            "        },\n" +
            "        \"data\": 1140,\n" +
            "        \"filename\": null,\n" +
            "        \"headers\": null\n" +
            "      },\n" +
            "      \"deletable\": false,\n" +
            "      \"integration_details\": {\n" +
            "        \"connections\": [],\n" +
            "        \"subjectCode\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant ID Column Name\",\n" +
            "            \"columnName\": \"PPID\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [\"2\",\"3\"],\n" +
            "            \"important_tokens\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"important_tokens_order\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"processed_id\": \"AAA-1-ZZZ\",\n" +
            "            \"type\": \"ppid\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant MRN Column Name\",\n" +
            "            \"columnName\": \"MRN\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"mrn\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"visitId\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Date Column\",\n" +
            "            \"columnName\": \"Visit# Visit Date\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitDate\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Label Column\",\n" +
            "            \"columnName\": \"Visit# Event Label\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitLabel\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Event Column\",\n" +
            "            \"columnName\": \"EVENT_NAME\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitCode\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matching_strategy\": null,\n" +
            "      \"name\": \"Specimen Data\",\n" +
            "      \"primary\": true,\n" +
            "      \"processed_id\": null,\n" +
            "      \"source_id\": \"0\",\n" +
            "      \"type\": \"OPENSPECIMEN\",\n" +
            "      \"valid\": true\n" +
            "    }";

    private String firstPositionGrowthString = "{\n" +
            "      \"connection_details\": {\n" +
            "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
            "        \"credentials\": {\n" +
            "          \"password\": \"test\",\n" +
            "          \"user\": \"test@test.com\"\n" +
            "        },\n" +
            "        \"data\": 1140,\n" +
            "        \"filename\": null,\n" +
            "        \"headers\": null\n" +
            "      },\n" +
            "      \"deletable\": false,\n" +
            "      \"integration_details\": {\n" +
            "        \"connections\": [],\n" +
            "        \"subjectCode\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant ID Column Name\",\n" +
            "            \"columnName\": \"PPID\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [\"2\",\"7\"],\n" +
            "            \"important_tokens\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"important_tokens_order\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"processed_id\": \"100-88888-ZZZ\",\n" +
            "            \"type\": \"ppid\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant MRN Column Name\",\n" +
            "            \"columnName\": \"MRN\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"mrn\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"visitId\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Date Column\",\n" +
            "            \"columnName\": \"Visit# Visit Date\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitDate\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Label Column\",\n" +
            "            \"columnName\": \"Visit# Event Label\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitLabel\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Event Column\",\n" +
            "            \"columnName\": \"EVENT_NAME\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitCode\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matching_strategy\": null,\n" +
            "      \"name\": \"Specimen Data\",\n" +
            "      \"primary\": true,\n" +
            "      \"processed_id\": null,\n" +
            "      \"source_id\": \"0\",\n" +
            "      \"type\": \"OPENSPECIMEN\",\n" +
            "      \"valid\": true\n" +
            "    }";

    private String firstPositionGrowthStringManyTokens = "{\n" +
            "      \"connection_details\": {\n" +
            "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
            "        \"credentials\": {\n" +
            "          \"password\": \"test\",\n" +
            "          \"user\": \"test@test.com\"\n" +
            "        },\n" +
            "        \"data\": 1140,\n" +
            "        \"filename\": null,\n" +
            "        \"headers\": null\n" +
            "      },\n" +
            "      \"deletable\": false,\n" +
            "      \"integration_details\": {\n" +
            "        \"connections\": [],\n" +
            "        \"subjectCode\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant ID Column Name\",\n" +
            "            \"columnName\": \"PPID\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [\"2\",\"7\",\"10\",\"12\",\"14\"],\n" +
            "            \"important_tokens\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"important_tokens_order\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"processed_id\": \"100-88888-ZZZ-QQ-YY-ZZ\",\n" +
            "            \"type\": \"ppid\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant MRN Column Name\",\n" +
            "            \"columnName\": \"MRN\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"mrn\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"visitId\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Date Column\",\n" +
            "            \"columnName\": \"Visit# Visit Date\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitDate\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Label Column\",\n" +
            "            \"columnName\": \"Visit# Event Label\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitLabel\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Event Column\",\n" +
            "            \"columnName\": \"EVENT_NAME\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitCode\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matching_strategy\": null,\n" +
            "      \"name\": \"Specimen Data\",\n" +
            "      \"primary\": true,\n" +
            "      \"processed_id\": null,\n" +
            "      \"source_id\": \"0\",\n" +
            "      \"type\": \"OPENSPECIMEN\",\n" +
            "      \"valid\": true\n" +
            "    }";

    private String middlePositionGrowthStringManyTokens = "{\n" +
            "      \"connection_details\": {\n" +
            "        \"URL\": \"https://stanford-biobank-url/rest/ng/query\",\n" +
            "        \"credentials\": {\n" +
            "          \"password\": \"test\",\n" +
            "          \"user\": \"test@test.com\"\n" +
            "        },\n" +
            "        \"data\": 1140,\n" +
            "        \"filename\": null,\n" +
            "        \"headers\": null\n" +
            "      },\n" +
            "      \"deletable\": false,\n" +
            "      \"integration_details\": {\n" +
            "        \"connections\": [],\n" +
            "        \"subjectCode\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant ID Column Name\",\n" +
            "            \"columnName\": \"PPID\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [\"2\",\"9\",\"12\",\"14\",\"16\"],\n" +
            "            \"important_tokens\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"important_tokens_order\": [\n" +
            "              \"0\",\n" +
            "              \"1\",\n" +
            "              \"2\"\n" +
            "            ],\n" +
            "            \"processed_id\": \"100-8888899-ZZZ-QQ-YY-ZZ\",\n" +
            "            \"type\": \"ppid\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Participant MRN Column Name\",\n" +
            "            \"columnName\": \"MRN\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"mrn\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"visitId\": [\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Date Column\",\n" +
            "            \"columnName\": \"Visit# Visit Date\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitDate\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Label Column\",\n" +
            "            \"columnName\": \"Visit# Event Label\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitLabel\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"columnLabel\": \"Specimen Collection Event Column\",\n" +
            "            \"columnName\": \"EVENT_NAME\",\n" +
            "            \"configurableColumn\": false,\n" +
            "            \"configurableType\": false,\n" +
            "            \"delimiter_positions\": [],\n" +
            "            \"important_tokens\": [],\n" +
            "            \"important_tokens_order\": [],\n" +
            "            \"processed_id\": null,\n" +
            "            \"type\": \"visitCode\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"matching_strategy\": null,\n" +
            "      \"name\": \"Specimen Data\",\n" +
            "      \"primary\": true,\n" +
            "      \"processed_id\": null,\n" +
            "      \"source_id\": \"0\",\n" +
            "      \"type\": \"OPENSPECIMEN\",\n" +
            "      \"valid\": true\n" +
            "    }";


    private ObjectMapper lastMapper = new ObjectMapper();
    private JsonNode lastPositionGrowthConfigJson = lastMapper.readTree(lastPositionGrowthString);
    private DataSourceConfig lastPositionGrowthConfig = new DataSourceConfig(lastPositionGrowthConfigJson);

    private ObjectMapper middleMapper = new ObjectMapper();
    private JsonNode middlePositionGrowthConfigJson = middleMapper.readTree(middlePositionGrowthString);
    private DataSourceConfig middlePositionGrowthConfig = new DataSourceConfig(middlePositionGrowthConfigJson);

    private ObjectMapper firstMapper = new ObjectMapper();
    private JsonNode firstPositionGrowthConfigJson = firstMapper.readTree(firstPositionGrowthString);
    private DataSourceConfig firstPositionGrowthConfig = new DataSourceConfig(firstPositionGrowthConfigJson);

    private ObjectMapper firstManyMapper = new ObjectMapper();
    private JsonNode firstManyPositionGrowthConfigJson = firstManyMapper.readTree(firstPositionGrowthStringManyTokens);
    private DataSourceConfig firstManyPositionGrowthConfig = new DataSourceConfig(firstManyPositionGrowthConfigJson);

    private ObjectMapper middleManyMapper = new ObjectMapper();
    private JsonNode middleManyPositionGrowthConfigJson = middleManyMapper.readTree(middlePositionGrowthStringManyTokens);
    private DataSourceConfig middleManyPositionGrowthConfig = new DataSourceConfig(middleManyPositionGrowthConfigJson);



    private TokenGrowthServiceImpl tokenGrowthService = new TokenGrowthServiceImpl();

    private TokenGrowthServiceImplTest() throws Exception {
    }

    @Test
    void processedIdGetterReturnsCorrectValue() {
        // Getting processed_id from the DatasourceConfig (model stored in elasticsearch)
        assertEquals("PRIMARY-123", lastPositionGrowthConfig.getProcessedId("ppid"));
    }

    @Test
    void detectTokenGrowth() throws Exception {
        // PRIMARY-123 is the processed_id, PRIMARY-1000 is a token growth case
        Boolean isThereGrowth = tokenGrowthService.detectTokenGrowth(lastPositionGrowthConfig, "PRIMARY-1000");
        assertEquals(true, isThereGrowth);
    }

    @Test
    void detectNoTokenGrowth() throws Exception {
        // PRIMARY-123 is the processed_id, PRIMARY-456 is not a token growth case
        Boolean isThereGrowth = tokenGrowthService.detectTokenGrowth(lastPositionGrowthConfig, "PRIMARY-456");
        assertEquals(false, isThereGrowth);
    }

    @Test
    void returnOriginalDelimPositionsGrowthInLastIndex() throws Exception {
        // When token growth is in the last position, no updates are needed, so the updated list returned should equal
        // the original delimiter position list.
        // processed_id = "PRIMARY-123", delimiter_positions = [6]
        // current_id = "PRIMARY-123456789", should return delimiter_positions = [6]

        String current_id = "PRIMARY-123456789";
        int growthIndex = 2;
        List<Integer> testOriginalDelimPositions = lastPositionGrowthConfig.getDelimiterPositions("ppid"); // [6]
        List<Integer> expectedDelimPositions = tokenGrowthService.correctedDelimiterPositions(lastPositionGrowthConfig, growthIndex, testOriginalDelimPositions, current_id);
        assertEquals(expectedDelimPositions, testOriginalDelimPositions);
    }

    @Test
    void returnCorrectedDelimPositionsGrowthInFirstIndex() throws Exception {
        // processed_id = "100-88888-ZZZ", delimiter_positions = [2,7]
        // current_id = "1000-8888-8ZZZ", the growth string, should return delimiter_positions: [3,8]
        String current_id = "1000-88888-ZZZ";

        List<Integer> originalDelimPositions = firstPositionGrowthConfig.getDelimiterPositions("ppid"); // [2,7]
        List<Integer> expectedDelimPositions = new ArrayList<>();
        expectedDelimPositions.add(3);
        expectedDelimPositions.add(8);
        List<Integer> testUpdatedDelimPositions = tokenGrowthService.correctedDelimiterPositions(firstPositionGrowthConfig, 0, originalDelimPositions, current_id);
        assertEquals(expectedDelimPositions, testUpdatedDelimPositions);
    }

    @Test
    void returnCorrectedDelimPositionsGrowthInMiddleIndex() throws Exception {
        // processed_id = "AAA-1-ZZZ", delimiter_positions = [2,3]
        // current_id = "15-110", the growth string, should return delimiter_positions: [2,5]
        String current_id = "15-110";

        List<Integer> originalDelimPositions = middlePositionGrowthConfig.getDelimiterPositions("ppid"); // [2,3]
        List<Integer> expectedDelimPositions = new ArrayList<>();
        expectedDelimPositions.add(2);
        expectedDelimPositions.add(5);
        List<Integer> testUpdatedDelimPositions = tokenGrowthService.correctedDelimiterPositions(middlePositionGrowthConfig, 1, originalDelimPositions, current_id);
        assertEquals(expectedDelimPositions, testUpdatedDelimPositions);
    }

    @Test
    void returnCorrectedDelimPositionsGrowthInFirstIndexManyTokens() throws Exception {
        // Same case as above but with many more delimiters / tokens and slightly larger growth
        // processed_id = "100-88888-ZZZ-QQ-YY-ZZ", delimiter_positions = [2,7,10,12,14]
        // current_id = "100000-88888-ZZZ-QQ-YY-ZZ", should return delimiter_positions = [5,10,13,15,17]
        String current_id = "100000-88888-ZZZ-QQ-YY-ZZ";

        List<Integer> originalDelimPositions = firstManyPositionGrowthConfig.getDelimiterPositions("ppid"); // [2,7,10,12,14]
        List<Integer> expectedDelimPositions = new ArrayList<>();
        expectedDelimPositions.add(5);
        expectedDelimPositions.add(10);
        expectedDelimPositions.add(13);
        expectedDelimPositions.add(15);
        expectedDelimPositions.add(17);
        List<Integer> testUpdatedDelimPositions = tokenGrowthService.correctedDelimiterPositions(firstManyPositionGrowthConfig, 0, originalDelimPositions, current_id);
        assertEquals(expectedDelimPositions, testUpdatedDelimPositions);
    }

    @Test
    void returnCorrectedDelimPositionsGrowthInMiddleIndexManyTokens() throws Exception {
        // Same case as above with token growth in a middle position
        // processed_id = "100-8888899-ZZZ-QQ-YY-ZZ", delimiter_positions = [2,9,12,14,16]
        // current_id = "100-88888100-ZZZ-QQ-YY-ZZ", should return delimiter_positions = [2,10,13,15,17]
        String current_id = "100-88888100-ZZZ-QQ-YY-ZZ";

        List<Integer> originalDelimPositions = middleManyPositionGrowthConfig.getDelimiterPositions("ppid"); // [2,9,12,14,16]
        List<Integer> expectedDelimPositions = new ArrayList<>();
        expectedDelimPositions.add(2);
        expectedDelimPositions.add(10);
        expectedDelimPositions.add(13);
        expectedDelimPositions.add(15);
        expectedDelimPositions.add(17);
        List<Integer> testUpdatedDelimPositions = tokenGrowthService.correctedDelimiterPositions(middleManyPositionGrowthConfig, 1, originalDelimPositions, current_id);
        assertEquals(expectedDelimPositions, testUpdatedDelimPositions);
    }

}



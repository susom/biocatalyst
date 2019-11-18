package edu.stanford.integrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.services.SampleFieldServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleFieldServiceImplTest {
    private String openSpecimenSource = "{\n" +
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
            "            \"columnName\": \"Specimen_Collection_Date\",\n" +
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

    private String osDataString = "{ \" Specimen for CP1140\": {\"columnLabels\":[\"PPID\",\"EMPI\",\"MRN\",\"MRN_Site_ID\",\"MRN_Site_Name\",\"Registration_Date\",\"cprId\",\"Date_of_Birth\",\"Gender\",\"Race\",\"Ethnicity\",\"Collection_Protocol_Short_Title\",\"Collection_Protocol_Title\",\"Specimen_Lineage\",\"Specimen_Id\",\"Specimen_Label\",\"Specimen_Barcode\",\"Specimen_Type\",\"Specimen_Available_Quantity\",\"Specimen_Activity_Status\",\"Specimen_Collection_Status\",\"Specimen_Formatted_Position\",\"Specimen_Container_Name\",\"Specimen_Root_Container_Name\",\"Specimen_Container_Hierarchy\",\"EVENT_NAME\",\"Specimen_Collection_Date\",\"Specimen_Collection_Group_Name\"],\"columnUrls\":[\"#/object-state-params-resolver?stateName=participant-detail.overview&objectName=cpr&key=id&value={{$cprId}}\",null,null,\"#/object-state-params-resolver?stateName=site-detail.overview&objectName=site&key=id&value={{$value}}\",\"#/object-state-params-resolver?stateName=site-detail.overview&objectName=site&key=name&value={{$value}}\",null,\"#/object-state-params-resolver?stateName=participant-detail.overview&objectName=cpr&key=id&value={{$value}}\",null,null,null,null,\"#/object-state-params-resolver?stateName=cp-detail.overview&objectName=cp&key=shortTitle&value={{$value}}\",\"#/object-state-params-resolver?stateName=cp-detail.overview&objectName=cp&key=title&value={{$value}}\",null,\"#/object-state-params-resolver?stateName=specimen-detail.overview&objectName=specimen&key=id&value={{$value}}\",\"#/object-state-params-resolver?stateName=specimen-detail.overview&objectName=specimen&key=id&value={{$specimenId}}\",null,null,null,null,null,null,\"#/object-state-params-resolver?stateName=container-detail.locations&objectName=container&key=name&value={{$value}}\",\"#/object-state-params-resolver?stateName=container-detail.overview&objectName=container&key=name&value={{$value}}\",null,null,null,\"#/object-state-params-resolver?stateName=visit-detail.overview&objectName=visit&key=name&value={{value}}\"],\"rows\":[[\"15-001\",null,null,null,null,\"03-09-2006 00:00\",\"29545\",\"12-27-1990 00:00\",\"Male\",\"Black or African American\",null,\"SLVP TMF\",\"SLVP Token Match FULL\",\"Aliquot\",\"971450\",\"V0-PBMC-2083\",null,\"PBMC\",\"100.0000\",\"Active\",\"Collected\",null,null,null,null,\"Day Zero\",\"03-09-2006 00:00\",\"15-001-Day Zero\"]],\"columnIndices\":null,\"dbRowsCount\":1}}";

    private ObjectMapper osMapper = new ObjectMapper();
    private JsonNode osConfigJson = osMapper.readTree(openSpecimenSource);
    private DataSourceConfig osConfig = new DataSourceConfig(osConfigJson);

    private ObjectMapper osDataMapper = new ObjectMapper();
    private JsonNode osDataJson = osDataMapper.readTree(osDataString);

    private String redcapSource = "{\n" +
            "                \"source_id\": \"0\",\n" +
            "                \"name\": \"Specimen Inventory Data\",\n" +
            "                \"type\": \"REDCAP\",\n" +
            "                \"connection_details\": {\n" +
            "                    \"URL\": \"\",\n" +
            "                    \"filename\": null,\n" +
            "                    \"headers\": null,\n" +
            "                    \"data\": \"14649\",\n" +
            "                    \"credentials\": {\n" +
            "                        \"user\": null,\n" +
            "                        \"password\": \"45259\"\n" +
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
            "                            \"important_tokens_order\": [\n" +
            "                                \"0\",\n" +
            "                                \"1\"\n" +
            "                            ],\n" +
            "                            \"growth_tokens\": [\n" +
            "                                \"1\"\n" +
            "                            ],\n" +
            "                            \"processed_id\": \"15-001\",\n" +
            "                            \"configurableColumn\": true,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"type\": \"mrn\",\n" +
            "                            \"columnLabel\": \"Participant MRN Column\",\n" +
            "                            \"columnName\": \"MRN\",\n" +
            "                            \"delimiter_positions\": [],\n" +
            "                            \"important_tokens\": [],\n" +
            "                            \"important_tokens_order\": [],\n" +
            "                            \"growth_tokens\": [],\n" +
            "                            \"processed_id\": null,\n" +
            "                            \"configurableColumn\": false,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"visitId\": [\n" +
            "                        {\n" +
            "                            \"type\": \"visitDate\",\n" +
            "                            \"columnLabel\": \"Visit Date Column\",\n" +
            "                            \"columnName\": \"visit_date\",\n" +
            "                            \"delimiter_positions\": [],\n" +
            "                            \"important_tokens\": [],\n" +
            "                            \"important_tokens_order\": [],\n" +
            "                            \"growth_tokens\": [],\n" +
            "                            \"processed_id\": null,\n" +
            "                            \"configurableColumn\": false,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"type\": \"visitCode\",\n" +
            "                            \"columnLabel\": \"Visit Code Column\",\n" +
            "                            \"columnName\": \"EVENT_NAME\",\n" +
            "                            \"delimiter_positions\": [],\n" +
            "                            \"important_tokens\": [],\n" +
            "                            \"important_tokens_order\": [],\n" +
            "                            \"growth_tokens\": [],\n" +
            "                            \"processed_id\": null,\n" +
            "                            \"configurableColumn\": false,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"type\": \"visitLabel\",\n" +
            "                            \"columnLabel\": \"Visit Label Column\",\n" +
            "                            \"columnName\": \"Visit# Event Label\",\n" +
            "                            \"delimiter_positions\": [],\n" +
            "                            \"important_tokens\": [],\n" +
            "                            \"important_tokens_order\": [],\n" +
            "                            \"growth_tokens\": [],\n" +
            "                            \"processed_id\": null,\n" +
            "                            \"configurableColumn\": false,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"type\": \"other\",\n" +
            "                            \"columnLabel\": \"Other Visit Column\",\n" +
            "                            \"columnName\": null,\n" +
            "                            \"delimiter_positions\": [],\n" +
            "                            \"important_tokens\": [],\n" +
            "                            \"important_tokens_order\": [],\n" +
            "                            \"growth_tokens\": [],\n" +
            "                            \"processed_id\": null,\n" +
            "                            \"configurableColumn\": false,\n" +
            "                            \"configurableType\": false,\n" +
            "                            \"removeable\": false\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"connections\": [\n" +
            "                        {\n" +
            "                            \"sourceId\": null,\n" +
            "                            \"subjectCodeType\": null,\n" +
            "                            \"visitId\": null,\n" +
            "                            \"matching_strategy\": null\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                \"matching_strategy\": null,\n" +
            "                \"processed_id\": null,\n" +
            "                \"deletable\": false,\n" +
            "                \"valid\": false,\n" +
            "                \"primary\": true\n" +
            "            },";

    private String rcDataString = "{\n" +
            "  \"REPORTS45259\": [\n" +
            "    {\n" +
            "      \"a_a1\": \"A*01:01:01:01\",\n" +
            "      \"a_a2\": \"A*24:02:01:01\",\n" +
            "      \"b_b1\": \"B*49:01:01\",\n" +
            "      \"b_b2\": \"B*51:01:01\",\n" +
            "      \"c_c1\": \"C*07:01:01\",\n" +
            "      \"c_c2\": \"C*07:50\",\n" +
            "      \"dqa_dqa11\": \"DQA1*01:03:01:01\",\n" +
            "      \"dqa_dqa12\": \"DQA1*02:01\",\n" +
            "      \"drb_drb11\": \"DRB1*07:01:01:01\",\n" +
            "      \"drb_drb12\": \"DRB1*13:01:01\",\n" +
            "      \"drb_drb3451\": \"DRB3*02:02:01:01\",\n" +
            "      \"drb_drb3452\": \"DRB4*01:03:01:01\",\n" +
            "      \"ethnicity\": \"Non-Hispanic\",\n" +
            "      \"gender\": \"Female\",\n" +
            "      \"race\": \"Caucasian or White\",\n" +
            "      \"redcap_repeat_instance\": \"\",\n" +
            "      \"redcap_repeat_instrument\": null,\n" +
            "      \"sampleid_sequencing\": \"MD1_01_1\",\n" +
            "      \"sid_barcode\": \"14.1.ACAGTAT\",\n" +
            "      \"subject_id\": \"15-001\",\n" +
            "      \"visit_date\": \"May 25th, 1985\",\n" +
            "      \"twin_status\": \"0\"\n" +
            "    }" +
            "  ]\n" +
            "}";


    private ObjectMapper rcMapper = new ObjectMapper();
    private JsonNode rcConfigJson = rcMapper.readTree(redcapSource);
    private DataSourceConfig rcConfig = new DataSourceConfig(rcConfigJson);

    private ObjectMapper rcDataMapper = new ObjectMapper();
    private JsonNode rcDataJson = rcDataMapper.readTree(rcDataString);

    private String csvSource = "{\n" + 
            "  \"connection_details\": {\n" + 
            "    \"URL\": null,\n" + 
            "    \"credentials\": null,\n" + 
            "    \"data\": \"12a3s4fd5f6gh7n8s9l0\",\n" + 
            "    \"filename\": \"test.csv\",\n" + 
            "    \"headers\": [\n" + 
            "      \"Subject Id\",\n" + 
            "      \"Visit ID\",\n" + 
            "      \"Gender\"\n" + 
            "    ]\n" + 
            "  },\n" + 
            "  \"deletable\": true,\n" + 
            "  \"integration_details\": {\n" + 
            "    \"connections\": [\n" + 
            "      {\n" + 
            "        \"matching_strategy\": null,\n" + 
            "        \"sourceId\": \"0\",\n" + 
            "        \"subjectCodeType\": \"ppid\",\n" + 
            "        \"visitId\": {\n" + 
            "          \"dateMatchingRangeInDays\": 0,\n" + 
            "          \"eventNameMapping\": {\n" + 
            "            \"destination\": \"Specimen Data Visit IDs\",\n" + 
            "            \"mapping\": {\n" + 
            "              \"W0\": \"Screen/Week 0\",\n" + 
            "              \"W12\": \"Week 12\",\n" + 
            "              \"W26\": \"Week 26\",\n" + 
            "              \"W52\": \"Week 52\"\n" + 
            "            },\n" + 
            "            \"source\": \"CSV Visit IDs\"\n" + 
            "          },\n" + 
            "          \"type\": \"visitDate\"\n" + 
            "        }\n" + 
            "      }\n" + 
            "    ],\n" + 
            "    \"subjectCode\": [\n" + 
            "      {\n" + 
            "        \"columnLabel\": \"Participant ID Column\",\n" + 
            "        \"columnName\": \"Subject Id\",\n" + 
            "        \"configurableColumn\": true,\n" + 
            "        \"configurableType\": true,\n" + 
            "        \"delimiter_positions\": [],\n" + 
            "        \"growth_tokens\": [\n" + 
            "          \"0\"\n" + 
            "        ],\n" + 
            "        \"important_tokens\": [\n" + 
            "          \"0\"\n" + 
            "        ],\n" + 
            "        \"important_tokens_order\": [\n" + 
            "          \"0\"\n" + 
            "        ],\n" + 
            "        \"processed_id\": \"144\",\n" + 
            "        \"removeable\": false,\n" + 
            "        \"type\": \"ppid\"\n" + 
            "      }\n" + 
            "    ],\n" + 
            "    \"visitId\": [\n" + 
            "      {\n" + 
            "        \"columnLabel\": \"Specimen Visit ID\",\n" + 
            "        \"columnName\": \"Visit ID\",\n" + 
            "        \"configurableColumn\": false,\n" + 
            "        \"configurableType\": true,\n" + 
            "        \"delimiter_positions\": [],\n" + 
            "        \"growth_tokens\": [],\n" + 
            "        \"important_tokens\": [],\n" + 
            "        \"important_tokens_order\": [],\n" + 
            "        \"processed_id\": null,\n" + 
            "        \"removeable\": true,\n" + 
            "        \"type\": \"visitDate\"\n" + 
            "      }\n" + 
            "    ]\n" + 
            "  },\n" + 
            "  \"matching_strategy\": null,\n" + 
            "  \"name\": \"test-csv-source\",\n" + 
            "  \"primary\": false,\n" + 
            "  \"processed_id\": null,\n" + 
            "  \"source_id\": \"1\",\n" + 
            "  \"type\": \"CSV\",\n" + 
            "  \"valid\": true\n" + 
            "}";

    private String csvData = "[\n" + 
            "            {\n" + 
            "              \"Gender\": \"m\",\n" + 
            "              \"Subject Id\": \"144\",\n" + 
            "              \"Visit ID\": \"W0\"\n" + 
            "            },\n" + 
            "            {\n" + 
            "              \"Gender\": \"f\",\n" + 
            "              \"Subject Id\": \"145\",\n" + 
            "              \"Visit ID\": \"W12\"\n" + 
            "            }\n" + 
            "          ]";
    
    private ObjectMapper csvMapper = new ObjectMapper();
    private JsonNode csvConfigJson = csvMapper.readTree(csvData);
    private DataSourceConfig csvConfig = new DataSourceConfig(
            csvMapper.readTree(csvSource)
    );



    SampleFieldServiceImplTest() throws IOException {
    }
    private SampleFieldServiceImpl sampleFieldService = new SampleFieldServiceImpl();


    // Openspecimen
    @Test
    void openspecimenIdSample() throws Exception {
        sampleFieldService.setDataSourceConfig(osConfig);
        sampleFieldService.setJson(osDataJson);
        assertEquals("15-001", sampleFieldService.sampleOpenspecimen("ppid", 0));
    }

    @Test
    void openspecimenVisitDateSample() throws Exception {
        sampleFieldService.setDataSourceConfig(osConfig);
        sampleFieldService.setJson(osDataJson);
        assertEquals("03-09-2006 00:00", sampleFieldService.sampleOpenspecimen("visitDate", 0));
    }

    // Redcap
    @Test
    void redcapIdSample() throws Exception {
        sampleFieldService.setDataSourceConfig(rcConfig);
        sampleFieldService.setJson(rcDataJson);
        assertEquals("15-001", sampleFieldService.sampleRedcap("ppid", 0));
    }


    @Test
    void redcapVisitDateSample() throws Exception {
        sampleFieldService.setDataSourceConfig(rcConfig);
        sampleFieldService.setJson(rcDataJson);
        assertEquals("May 25th, 1985", sampleFieldService.sampleRedcap("visitDate", 0));
    }

    @Test
    void csvIdSample() throws Exception {
        sampleFieldService.setDataSourceConfig(csvConfig);
        sampleFieldService.setJson(csvConfigJson); // since data is stored with configuration, use csvConfigJson
        assertEquals("144", sampleFieldService.sampleCsv("ppid", 0));
    }

    @Test
    void csvVisitDateSample() throws Exception {
        sampleFieldService.setDataSourceConfig(csvConfig);
        sampleFieldService.setJson(csvConfigJson); // since data is stored with configuration, use csvConfigJson
        assertEquals("W0", sampleFieldService.sampleCsv("visitDate", 0));
    }
}
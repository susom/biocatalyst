package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.util.StringJsonToClass;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchRestClientTest {

  Util utilities = new Util();

  @Autowired
  StringJsonToClass mapping;

  String NullFilterValues = "{\n" +
      "  \"filters\": [\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_aliquot_note\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 0\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_arm_id\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 1\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_cryotubes\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 2\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterValues\": [\n" +
      "        null\n" +
      "      ],\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 3\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_frz\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 4\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_par\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 5\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_outcome\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 6\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_tech\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 7\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_clinicalbarcode_original\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 8\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_clinical_barcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 9\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_cs10_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 10\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_datereceived\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 11\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_date_sx\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 12\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_dob\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 13\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_ethnnew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 14\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragmens_cs10_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 15\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragments_oct_he_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 16\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_labs\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 17\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_oct_he_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 18\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_qc_detail\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 19\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_racenew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 20\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_redcap_data_access_group\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 21\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sample_alias\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 22\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_sex\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 23\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sitename\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 24\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimenbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 25\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimentype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 26\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_storagetype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 27\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_studygroup\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 28\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_subject_id\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 29\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_subject_type_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 30\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_collected_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 31\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_qc\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 32\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_wholeblood_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 33\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_syn_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 34\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_unifiedbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 35\n" +
      "    }\n" +
      "  ]\n" +
      "}";

  String StringTest = "{\n" +
      "  \"filters\": [\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_aliquot_note\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 0\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_arm_id\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 1\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_cryotubes\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 2\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 3\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_frz\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterType\": \"include\",\n" +
      "      \"filterValues\": null,\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 4\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_par\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 5\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_outcome\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 6\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_tech\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"filterValue\": \"Quick\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 7\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_clinicalbarcode_original\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"filterValue\": \"300\",\n" +
      "      \"selectedFilterOperation\": \"contains\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 8\n" +
      "    }," +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_clinical_barcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"filterValue\": \"300\",\n" +
      "      \"selectedFilterOperation\": \"notcontains\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 9\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_cs10_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 10\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_datereceived\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 11\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_date_sx\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 12\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_dob\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 13\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_ethnnew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 14\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragmens_cs10_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 15\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragments_oct_he_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 16\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_labs\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 17\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_oct_he_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 18\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_qc_detail\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 19\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_racenew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 20\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_redcap_data_access_group\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 21\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sample_alias\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 22\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_sex\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 23\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sitename\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 24\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimenbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 25\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimentype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 26\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_storagetype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 27\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_studygroup\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 28\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_subject_id\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 29\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_subject_type_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 30\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_collected_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 31\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_qc\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 32\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_wholeblood_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 33\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_syn_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 34\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_unifiedbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 35\n" +
      "    }\n" +
      "  ]\n" +
      "}";

  String MultipleFilterValues = "{\n" +
      "  \"filters\": [\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_aliquot_note\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 0\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_arm_id\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 1\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_cryotubes\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterType\": \"include\",\n" +
      "      \"filterValues\": null,\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 2\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterType\": \"include\",\n" +
      "      \"filterValues\": null,\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 3\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_frz\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterType\": \"include\",\n" +
      "      \"filterValues\": null,\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 4\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_frags_par\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"filterType\": \"include\",\n" +
      "      \"filterValue\": 4,\n" +
      "      \"filterValues\": [\n" +
      "        4\n" +
      "      ],\n" +
      "      \"selectedFilterOperation\": \"<\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 5\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_outcome\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 6\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_biop_tech\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 7\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_clinicalbarcode_original\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 8\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_clinical_barcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 9\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_cs10_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 10\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_datereceived\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 11\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_date_sx\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 12\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_dob\",\n" +
      "      \"dataType\": \"datetime\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 13\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_ethnnew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 14\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragmens_cs10_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 15\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_fragments_oct_he_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 16\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_labs\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 17\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_oct_he_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 18\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_qc_detail\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 19\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_racenew\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 20\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_redcap_data_access_group\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 21\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sample_alias\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 22\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_sex\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 23\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_sitename\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 24\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimenbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 25\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_specimentype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 26\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_storagetype\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 27\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_studygroup\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 28\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_subject_id\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 29\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_subject_type_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 30\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_collected_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 31\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_qc\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 32\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_synovial_wholeblood_v2\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 33\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s1_syn_fragments_num_v2\",\n" +
      "      \"dataType\": \"number\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 34\n" +
      "    },\n" +
      "    {\n" +
      "      \"dataField\": \"j88888888888888_s0_unifiedbarcode\",\n" +
      "      \"dataType\": \"string\",\n" +
      "      \"visible\": true,\n" +
      "      \"visibleIndex\": 35\n" +
      "    }\n" +
      "  ]\n" +
      "}";

  private static final Logger logger = Logger.getLogger(IntegrationClient.class);

  @Test
  public void NullFilterValues() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(NullFilterValues);
    List<Object> list = mapper.treeToValue(jsonNode.get("filters"), ArrayList.class);
    utilities.ProcessFilters(list);
    GenerateQuery(utilities.ElasticFilter(), utilities.ElasticMustNot());
  }

  @Test
  public void StringTests() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(StringTest);
    List<Object> list = mapper.treeToValue(jsonNode.get("filters"), ArrayList.class);
    utilities.ProcessFilters(list);
    GenerateQuery(utilities.ElasticFilter(), utilities.ElasticMustNot());
  }

  @Test
  public void MultiFilterValues() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(this.MultipleFilterValues);
    List<Object> list = mapper.treeToValue(jsonNode.get("filters"), ArrayList.class);
    utilities.ProcessFilters(list);
    GenerateQuery(utilities.ElasticFilter(), utilities.ElasticMustNot());
  }

  public void GenerateQuery(JSONArray filterArray, JSONArray mustNotArray) {
    JSONObject obj = new JSONObject();
    obj.put("query", new JSONObject()
        .put("bool", new JSONObject()
            .put("must_not", mustNotArray)
            .put("filter", filterArray))
    );
    logger.debug(obj);
  }

}

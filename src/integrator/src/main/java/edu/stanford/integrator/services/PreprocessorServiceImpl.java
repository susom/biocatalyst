package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.clients.LocalStorageClient;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.config.IntegrationConfig;
import edu.stanford.integrator.config.IntegrationConfigDBProxy;
import edu.stanford.integrator.importer.NoMoreDataException;
import edu.stanford.integrator.importer.OPENSPECIMENImporter;
import edu.stanford.integrator.importer.REDCAPImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PreprocessorServiceImpl implements PreprocessorService {
  private static final Logger LOGGER = Logger.getLogger(PreprocessorService.class.getName());

  @Autowired
  private IntegrationConfigDBProxy integrationConfigDBProxy;

  @Autowired
  OPENSPECIMENImporter OsImporter;

  @Autowired
  REDCAPImporter RedCapImporter;

  @Autowired
  SampleFieldService sampleFieldService;

  @Autowired
  LocalStorageClient storage;

  private String configId;
  private String idColumnNamePpid;
  private String idColumnNameMrn;
  private String connectToPrimaryIdType;

  public PreprocessorServiceImpl() {
    this.configId = "";
    this.idColumnNamePpid = "";
    this.idColumnNameMrn = "";
    this.connectToPrimaryIdType = "";
  }

  public void setConfigId(String value) {
    this.configId = value;
  }

  public void setIdColumnNamePpid(String value) {
    this.idColumnNamePpid = value;
  }

  public void setIdColumnNameMrn(String value) {
    this.idColumnNameMrn = value;
  }

  private IntegrationConfig readConfiguration(String configID) throws Exception {
    final JsonNode configJson = integrationConfigDBProxy.getIntegrationConfig(configID);

    if (configJson == null) {
      LOGGER.log(Level.SEVERE, "The configuration for integration " + configID + " is missing.");
      throw new Exception("The configuration for integration " + configID + " is missing.");
    }

    return new IntegrationConfig(configJson);
  }

  public String transformId(DataSourceConfig sourceConfig, String rawId, String idType, Boolean applyTokenOrder, List<Integer> tokenGrowthDelimiterPositions) throws Exception {
    // If this is an MRN, just return the value passed in; no manipulation necessary
    if (idType.equalsIgnoreCase("MRN")) {
      return rawId;
    }

    // Otherwise, for PPID, transform
    // If there's token growth, use the updated delimiter position, otherwise pull from sourceConfig
    String delimmed = "";
    if (tokenGrowthDelimiterPositions != null) {
      delimmed = applyDelimiter(rawId, sourceConfig, tokenGrowthDelimiterPositions);
    } else {
      delimmed = applyDelimiter(rawId, sourceConfig, sourceConfig.getDelimiterPositions("PPID"));
    }
    String[] tokenizedDelimmed = tokenize(delimmed, false);
    String[] impTokens = this.extractImportantTokens(tokenizedDelimmed, sourceConfig.getImportantTokens("PPID"));
    // applyTokenOrder is false the first pass - when trying to "shuffle the deck" and get the same string
    // applyTokenOrder is true the second pass - when trying to evaluate the important token order
    String join_id = "";
    if (applyTokenOrder) {
      String[] impTokensOrdered = this.applyImportantTokenOrder(impTokens, sourceConfig.getImportantTokensOrder("PPID"));
      join_id = finishJoinId(impTokensOrdered);
    } else {
      join_id = finishJoinId(impTokens);
    }

    // wrap join_id in quotes if it's in use by important token order (importUtil)
    String join_idCurated = "";
    if (applyTokenOrder) {
      join_idCurated = '"' + join_id + '"';
      return join_idCurated;
    } else {
      return join_id;
    }
  }

  public Map<String, Object> determineImportantTokensOrder(String remoteUser, String sourceId) throws Exception {
    if (sourceId.equals("0")) {
      LOGGER.log(Level.SEVERE,
          "Evaluation request for primary datasource. Not going to evaluate matching strategy with primary source to itself");
      throw new Exception("Not going to evaluate matching strategy with primary source to itself");
    }

    final IntegrationConfig integrationConfig = readConfiguration(this.configId);
    final DataSourceConfig primarySourceConfig = integrationConfig.getSourceConfig("0");

    // I'm using the term "secondarySourceConfig", but this can be any source ID
    // that is not the primary
    final DataSourceConfig secondarySourceConfig = integrationConfig.getSourceConfig(sourceId);
    String idType = secondarySourceConfig.getConnectToPrimaryIdType();
    int pCounter = 0;
    int npCounter = 0;

    // Pull all the data once
    String primarySourceFullDataString = getFullDataAsJsonString(remoteUser, primarySourceConfig);
    String secondarySourceFullDataString = getFullDataAsJsonString(remoteUser, secondarySourceConfig);

    // Get a sample for each
    String primarySampleRaw = getOneId(primarySourceConfig, primarySourceFullDataString, pCounter);
    String secondarySampleRaw = getOneId(secondarySourceConfig, secondarySourceFullDataString, npCounter);
    LOGGER.log(Level.INFO, "primarySampleRaw:" + primarySampleRaw);
    LOGGER.log(Level.INFO, "secondarySampleRaw:" + secondarySampleRaw);

    // Transform by applying instructions (delimiter_positions, important_tokens) to get two arrays of tokens to compare. We don't have important_tokens_order yet
    // Also, we're not dealing with token growth here, so pass null for tokenGrowthDelimiterPositions
    String primarySample = transformId(primarySourceConfig, primarySampleRaw, idType, false, null);
    String secondarySample = transformId(secondarySourceConfig, secondarySampleRaw, idType, false, null);
    LOGGER.log(Level.INFO, "primarySample processed: " + primarySample);
    LOGGER.log(Level.INFO, "secondarySample processed: " + secondarySample);

    // Skips the loops if both initially sampled tokens arrays match
    // Keep track of raw and sorted. Raw for the eventual important token order, sorted to find a match with 'shuffle the deck'
    String[] rawTokenizedPrimary = tokenize(primarySample, false);
    String[] sortedTokenizedPrimary = rawTokenizedPrimary.clone();
    Arrays.sort(sortedTokenizedPrimary);
    String[] rawTokenizedSecondary = tokenize(secondarySample, false);
    String[] presortedTokenizedSecondary = rawTokenizedSecondary.clone();
    String[] sortedTokenizedSecondary = rawTokenizedSecondary.clone();
    Arrays.sort(sortedTokenizedSecondary);
    LOGGER.log(Level.INFO, "Pre shuffle the deck, sorted and split into tokens, primary:" + Arrays.toString(sortedTokenizedPrimary));
    LOGGER.log(Level.INFO, "Pre shuffle the deck, raw secondary: " + Arrays.toString(rawTokenizedSecondary));
    LOGGER.log(Level.INFO, "Pre shuffle the deck, sorted and split into tokens, secondary:" + Arrays.toString(sortedTokenizedSecondary));

    if (Arrays.equals(sortedTokenizedPrimary, sortedTokenizedSecondary)) {
      LOGGER.log(Level.INFO, "Transformed and sorted tokens match. No need to shuffle.");
    }

    // DRY THIS UP
    else {
      // Begin 'shuffle the deck' algorithm
      // Ensure primary and secondary source strings match. Continue to iterate through source 1 until it's found
      String primarySampleUpdatedTransformed = "";
      String secondarySampleUpdatedTransformed = "";
      // Then try iterating through non-primary samples using the original primary sample until a match is found
      while (!Arrays.equals(sortedTokenizedPrimary, sortedTokenizedSecondary)) { // not equals, regardless of order (because we don't know the token order)
        LOGGER.log(Level.INFO, "[SHUFFLE NON-PRIMARY] Processed tokens don't match, executing 'shuffling the deck' algorithm. Running through more non-primary samples until a match is found.");
        npCounter++;
        primarySample = getOneId(primarySourceConfig, primarySourceFullDataString, pCounter);
        primarySampleUpdatedTransformed = transformId(primarySourceConfig, primarySample, idType, false, null);
        sortedTokenizedPrimary = tokenize(primarySampleUpdatedTransformed, false);
        Arrays.sort(sortedTokenizedPrimary);
        try {
          secondarySample = getOneId(secondarySourceConfig, secondarySourceFullDataString, npCounter);
        } catch (NoMoreDataException ex) {
          LOGGER.log(Level.INFO, "[SHUFFLE NON-PRIMARY FINISHED] Could not find a non-primary sample to match with primary.");
          break;
        }
        secondarySampleUpdatedTransformed = transformId(secondarySourceConfig, secondarySample, idType, false, null);
        presortedTokenizedSecondary = tokenize(secondarySampleUpdatedTransformed, false);
        sortedTokenizedSecondary = presortedTokenizedSecondary.clone();
        Arrays.sort(sortedTokenizedSecondary);
        LOGGER.log(Level.INFO, "[SHUFFLE NON-PRIMARY] Primary: " + Arrays.toString(sortedTokenizedPrimary));
        LOGGER.log(Level.INFO, "[SHUFFLE NON-PRIMARY] Non-Primary: " + Arrays.toString(sortedTokenizedSecondary));
      }

      npCounter = 0;
      while (!Arrays.equals(sortedTokenizedSecondary, sortedTokenizedPrimary)) {
        LOGGER.log(Level.INFO, "[SHUFFLE PRIMARY] Running through all the primary samples until a match is found.");
        pCounter++;
        primarySample = getOneId(primarySourceConfig, primarySourceFullDataString, pCounter);
        primarySampleUpdatedTransformed = transformId(primarySourceConfig, primarySample, idType, false, null);
        sortedTokenizedPrimary = tokenize(primarySampleUpdatedTransformed, false);
        Arrays.sort(sortedTokenizedPrimary);

        secondarySample = getOneId(secondarySourceConfig, secondarySourceFullDataString, npCounter);
        secondarySampleUpdatedTransformed = transformId(secondarySourceConfig, secondarySample, idType, false, null);
        presortedTokenizedSecondary = tokenize(secondarySampleUpdatedTransformed, false);
        sortedTokenizedSecondary = presortedTokenizedSecondary.clone();
        Arrays.sort(sortedTokenizedSecondary);
        LOGGER.log(Level.INFO, "[SHUFFLE PRIMARY] Primary: " + Arrays.toString(sortedTokenizedPrimary));
        LOGGER.log(Level.INFO, "[SHUFFLE PRIMARY] Non-Primary: " + Arrays.toString(sortedTokenizedSecondary));
      }

      LOGGER.log(Level.INFO, "[SHUFFLE FINISHED] Finished 'shuffle the deck' algorithm to get two sets of tokens that will match.");
      LOGGER.log(Level.INFO, "Sorted Final Tokenized Primary: " + Arrays.toString(sortedTokenizedPrimary));
      LOGGER.log(Level.INFO, "Sorted Final Tokenized Secondary: " + Arrays.toString(sortedTokenizedSecondary));
    }

    LOGGER.log(Level.INFO, "Unsorted Final Tokenized Primary (in the order the original ID was configured): " + Arrays.toString(sortedTokenizedPrimary));
    LOGGER.log(Level.INFO, "This is used as the base for token growth order.");

    // Determine Secondary Source Reordering
    // Use presorted, not sorted tokens
    LOGGER.log(Level.INFO, "Unsorted Final Tokenized Secondary, without being ordered: " + Arrays.toString(sortedTokenizedSecondary));
    LOGGER.log(Level.INFO, "Evaluating Secondary Token Order");

    Integer[] secondaryTokenOrder = evaluateAndWriteImportantTokensOrder(sortedTokenizedPrimary, sortedTokenizedSecondary);
    LOGGER.log(Level.INFO, "Secondary Token Order: " + Arrays.toString(secondaryTokenOrder));
    String[] importantSecondaryTokensOrdered = this.applyImportantTokenOrder(sortedTokenizedSecondary,
        secondaryTokenOrder);
    LOGGER.log(Level.INFO, "Important Secondary Tokens Ordered: " + Arrays.toString(importantSecondaryTokensOrdered));

    // Create Join String
    final String finalJoinPrimaryString = finishJoinId(sortedTokenizedPrimary);
    final String finalJoinSecondaryString = finishJoinId(importantSecondaryTokensOrdered);

    LOGGER.log(Level.INFO, "Final JOIN String Primary: " + finalJoinPrimaryString);
    LOGGER.log(Level.INFO, "Final JOIN String Secondary: " + finalJoinSecondaryString);

    Map<String, Object> result = new HashMap<String, Object>();

    // Return important_tokens_order to the UI, which is written to the backend model using biosearch-proxy
    result.put("important_tokens_order", secondaryTokenOrder);

    return result;
  }

  public String getFullDataAsJsonString(String remoteUser, DataSourceConfig sourceConfig) throws Exception {
    String sourceType = sourceConfig.getSourceType().toLowerCase();
    String sourceIdColumnName = "";

    if (connectToPrimaryIdType.equalsIgnoreCase("mrn")) {
      sourceIdColumnName = sourceConfig.getIdColumnNameMrn();
      LOGGER.log(Level.INFO, "ID Column Name: " + sourceIdColumnName);
      this.setIdColumnNameMrn(sourceIdColumnName);
    } else { // default to PPID if not explicitly MRN. Secondary sources should only set
      // either Ppid or Mrn column
      sourceIdColumnName = sourceConfig.getIdColumnNamePpid();
      LOGGER.log(Level.INFO, "ID Column Name: " + sourceIdColumnName);
      this.setIdColumnNamePpid(sourceIdColumnName);
    }
    if (sourceType.equals("openspecimen")) {
      // getOneSpecimenQuery is in application.properties
      String openSpecimenData = OsImporter.getSpecimenData(sourceConfig);
      //LOGGER.log(Level.INFO, "OpenSpecimen Data: " + openSpecimenData);
      return openSpecimenData;
    } else if (sourceType.equals("redcap")) {
      String redcapData = RedCapImporter.getReportJSON(sourceConfig, remoteUser);
      //LOGGER.log(Level.INFO, "RedCap Data: " + redcapData);
      return redcapData;
    } else if (sourceType.equals("csv")) {
      String id = sourceConfig.getCsvStorageID();
      JsonNode rows = storage.getData(id, remoteUser);
      return rows.toString();
    } else {
      LOGGER.log(Level.SEVERE, "Preprocessor could not get data from remote source: " + sourceType);
      throw new Exception("Preprocessor could not get data from remote source: " + sourceType);
    }
  }

  public DataSourceConfig getDataSourceConfig(String remoteUser, String sourceId) throws Exception {
    final IntegrationConfig integrationConfig = readConfiguration(this.configId);
    return integrationConfig.getSourceConfig(sourceId);
  }

  public String getOneId(DataSourceConfig sourceConfig, String jsonString, int counter) throws Exception {
    String sourceType = sourceConfig.getSourceType().toLowerCase();
    String returnValue = "";
    ObjectMapper mapper = new ObjectMapper();

    switch (sourceType) {
      case "openspecimen":
        // getOneSpecimenQuery is in application.properties
        //String openSpecimenData = OsImporter.getOneSpecimenData(sourceConfig, counter);
        //LOGGER.log(Level.INFO, "OpenSpecimen Data: " + openSpecimenData);
        JsonNode openSpecimenJson = mapper.readTree(jsonString);
        sampleFieldService.setDataSourceConfig(sourceConfig);
        sampleFieldService.setJson(openSpecimenJson);
        returnValue = sampleFieldService.sampleOpenspecimen("ppid", counter);
        LOGGER.log(Level.INFO, "OpenSpecimen Sample: " + returnValue);
        break;
      case "redcap":
        //LOGGER.log(Level.INFO, "RedCap Data: " + redcapData);
        JsonNode redcapJson = mapper.readTree(jsonString);
        sampleFieldService.setDataSourceConfig(sourceConfig);
        sampleFieldService.setJson(redcapJson);
        returnValue = sampleFieldService.sampleRedcap("ppid", counter);
        LOGGER.log(Level.INFO, "RedCap Sample: " + returnValue);
        break;
      case "csv":
        sampleFieldService.setDataSourceConfig(sourceConfig);
        JsonNode rows = mapper.readTree(jsonString);
        sampleFieldService.setJson(rows);
        returnValue = sampleFieldService.sampleCsv("ppid", counter);
        LOGGER.log(Level.INFO, "CSV Sample: " + returnValue);
        break;
      default:
        LOGGER.log(Level.SEVERE, "Could not determine source type, returning null for getOneId method");
    }
    return returnValue;
  }

  public String[] tokenize(String rawString, Boolean stripLeadingZeros) throws Exception {
    // Split into tokens for nonalphanumeric characters
    String[] stringTokens = rawString.split("[^0-9A-Za-z]");
    // You should NOT do this if you're trying to see if there's token growth, so that service has this set to false for now
    if (stripLeadingZeros) {
      for (int i = 0; i < stringTokens.length; i++) {
        stringTokens[i] = stringTokens[i].replaceFirst("^0+(?!$)", "");
      }
    }
    return stringTokens;
  }

  public String applyDelimiter(String rawString, DataSourceConfig sourceConfig, List<Integer> delimiterPositionList) throws Exception {
    // Take the rawString as input
    // Pull the Elasticsearch configuration for sourceConfig and sourceId
    // delimiter_positions is passed in because it may be overridden from what's in the config due to token growth
    // Return a string with the delimiters in the correct places

    // Currently, we only support applying a delimiter to PPIDs, so this is hard coded
    String type = "PPID";

    // Logging only, not used elsewhere in method
    List<Integer> importantTokensList = sourceConfig.getImportantTokens(type);
    String strippedString = rawString.replaceAll("[^\\w']+", "");
    LOGGER.log(Level.INFO,
        "Source " + sourceConfig.getSourceId() + ", Rawstring: " + rawString + ", Delim list: " + delimiterPositionList
            + ", Important tokens: " + importantTokensList);

    StringBuilder builder = new StringBuilder();

    // add delimiters back based on delimiterPositionsList
    char[] charArray = strippedString.toCharArray();
    for (int i = 0; i < charArray.length; i++) {
      if (delimiterPositionList.contains(i)) {
        builder.append("-");
      }
      builder.append(charArray[i]);
    }
    return builder.toString();
  }

  public String[] extractImportantTokens(String[] tokens, List<Integer> importantTokensList)
      throws Exception {
    // Create a string based only on the important tokens
    // e.g. AAA-123-BBB if the desired result is only important_tokens: [0,1],
    // important_tokens_order: [0,1] the
    // string will be returned as AAA-123
    // important_tokens_order: [1,0] should return 123-AAA

    // First, filter tokens to only the important tokens as defined by the source
    // config's important_tokens
    List<String> impTokens = new ArrayList<>();

    for (int i = 0; i < importantTokensList.size(); i++) {
      Integer tokenIndex = importantTokensList.get(i);
      if (tokenIndex < tokens.length) {
        impTokens.add(tokens[tokenIndex]);
      }
    }

    String[] results = new String[impTokens.size()];
    return impTokens.toArray(results);
  }

  public String[] applyImportantTokenOrder(String[] tokens, Integer[] newOrder) throws Exception {
    // Create a string based only on the important tokens
    // e.g. AAA-123-BBB if the desired result is only important_tokens: [0,1],
    // important_tokens_order: [0,1] the
    // string will be returned as AAA-123
    // important_tokens_order: [1,0] should return 123-AAA
    String[] results = new String[newOrder.length];

    for (int index = 0; index < tokens.length; index++) {
      Integer position = newOrder[index];
      if (position < 0) {
        LOGGER.log(Level.WARNING, "Swallowing: Invalid Position found during Token Reordering.");
        continue;
      }
      results[position] = tokens[index];
    }

    return results;
  }

  private static String finishJoinId(String[] tokens) throws Exception {
    String finalJoinId = "";
    for (String token : tokens) {
      finalJoinId += token;
      finalJoinId += "-";
    }
    // Strip last hyphen
    finalJoinId = finalJoinId.substring(0, finalJoinId.length() - 1);
    return finalJoinId;
  }

  private static Integer[] evaluateAndWriteImportantTokensOrder(String[] tokenizedPrimary,
                                                                String[] tokenizedSecondary) throws Exception {
    // evaluate the correct order of tokens from S0 to S1 such that the result is an
    // exact match
    // Save this to Elasticsearch as two lists, e.g.:
    // AAA-001-15, 15-1
    // Desired JOIN ID: 1-15
    // primary_important_tokens_order: [0,1], nonprimary_important_tokens_order:
    // [1,0]
    // This is a unique index (unrelated to important_tokens/delimiter_positions).

    // The length of the primary important_tokens_order will always equal the length
    // of the important_tokens list
    // and since now everything is integrating with the primary, it's always
    // sequential based on the number of tokens.
    // This may change in the future as integrations, which is why we set it for
    // primary.

    // Assumptions
    // #1 The tokens are unique, and therefore it is okay to grab the first match as
    // it is assumed,
    // that there will be no matches downstream

    // #2 There should be n unique numbers where N is the length of the
    // tokenizedSecondary array
    if (tokenizedPrimary.length <= 0) {
      System.out.println(String.format("No primary tokens to order."));
      return null;
    }

    if (tokenizedSecondary.length <= 0) {
      System.out.println(String.format("No secondary tokens to order."));
      return null;
    }

    int totalMatches = 0;
    Integer[] results = new Integer[tokenizedSecondary.length];

    for (int index = 0; index < results.length; index++) {
      results[index] = -1;
    }

    for (int index = 0; index < tokenizedSecondary.length; index++) {
      String secondaryToken = tokenizedSecondary[index];

      int matches = 0; // See Assumption #1;
      for (int endex = 0; endex < tokenizedPrimary.length; endex++) {
        String primaryToken = tokenizedPrimary[endex];
        if (secondaryToken.equals(primaryToken)) {
          if (matches > 1) { // See Assumption #1;
            System.out.println(String.format("Duplicate Token Matches found when establishing order:\n"
                + "Primary Token: %s \n" + "Primary Token Index: %s \n" + "Secondary Token: %s \n"
                + "Secondary Token Index: %s", primaryToken, endex, secondaryToken, index));
          }

          results[index] = (matches == 0) ? endex : results[index];
          totalMatches++;
          matches++;
        }

      }
    }

    // There should be n unique numbers where N is the length of the
    // tokenizedSecondary array
    // See Assumption #2;
    Set<Integer> removeDuplicates = new HashSet<>(Arrays.asList(results));
    for (Integer value : removeDuplicates) {
      if (value == -1) {
        System.out.println(String.format("Unable to match a token from the Secondary to the Primary:"));
      }
    }
    if (removeDuplicates.size() != tokenizedSecondary.length) {
      System.out.println(String.format(
          "Uneven Token Matches found when establishing order:\n"
              + "Total Matches (Possible Duplicates) : %s \n" + "Secondary Token Count: %s",
          totalMatches, tokenizedSecondary.length));
    }
    if (totalMatches != tokenizedSecondary.length) {
      System.out.println(String.format("Uneven Token Matches found when establishing order:\n"
          + "Total Matches : %s \n" + "Secondary Token Count: %s", totalMatches, tokenizedSecondary.length));
    }

    return results;
  }

  public Map<String, Object> createTokenizationAndRawStringObject(String rawString) throws Exception {
    // Split into tokens for nonalphanumeric characters
    String[] tokensList = tokenize(rawString, false);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("tokens", tokensList);
    result.put("raw", rawString);
    return result;
  }
}

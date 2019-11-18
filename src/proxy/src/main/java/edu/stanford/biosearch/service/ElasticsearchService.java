package edu.stanford.biosearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.config.DefaultExceptionHandler;
import edu.stanford.biosearch.data.elasticsearch.*;
import edu.stanford.biosearch.model.bookmark.Bookmark;
import edu.stanford.biosearch.model.configuration.*;
import edu.stanford.biosearch.model.integrateddata.BasicQuery;
import edu.stanford.biosearch.util.Base64Util;
import edu.stanford.biosearch.util.RemoteUser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ElasticsearchService {
  @Autowired
  NetworkClient networkClient;

  @Autowired
  CompletionSuggestionClient completionSuggestionClient;

  @Autowired
  ConfigurationClient configurationClient;

  @Autowired
  MappingClient mappingClient;

  @Autowired
  RoleClient roleClient;

  @Autowired
  ScrollClient scrollClient;

  @Autowired
  BookmarkClient bookmarkClient;

  @Autowired
  IntegrationClient integrationClient;

  @Autowired
  RemoteUser request;

  @Autowired
  public Base64Util base64Util;

  @Autowired
  BeanFactory beans;

  private static final Logger logger = Logger.getLogger(ElasticsearchService.class);

  public Map<String, Object> getCompletionSuggestion(String index, String field, String prefix) throws Exception {
    return completionSuggestionClient.getCompletionSuggestion(index, field, prefix);
  }

  public Map<String, Credential> credentialMap = new HashMap<String, Credential>();

  public Map<String, Object> getConfigs(List<String> configs) throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    Map<String, Object> result = configurationClient.getConfigs(configs);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> hits = (Map<String, Object>) responseBody.get(NetworkClient.HITS);
    List<Map<String, Object>> hitsArray = (List<Map<String, Object>>) hits.get(NetworkClient.HITS);
    for (Map<String, Object> hit : hitsArray) {
      DataSet dataSet = mapper.convertValue(hit.get(NetworkClient.SOURCE), DataSet.class);
      List<DataSource> dataSources = dataSet.getDataSources();
      for (DataSource dataSource : dataSources) {
        ConnectionDetails connDetails = dataSource.getConnDetails();
        Credential cred = connDetails.getCredential();
        credentialMap.put(dataSet.getId().concat("-" + dataSource.getSource_id()), cred);
        if (!dataSource.getType().equals(SourceType.REDCAP)) {
          connDetails.setCredential(null);
        }
      }
      hit.put(NetworkClient.SOURCE, dataSet);
    }

    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }

    return result;
  }

  public Map<String, Object> createConfig(DataSet dataSet) throws Exception {
    String document = "j".concat(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()).toLowerCase());
    dataSet.setId(document);

    // set connection details
    for (DataSource dataSource : dataSet.getDataSources()) {
      String message = dataSource.getType() + ". Specimen source? " + dataSource.isPrimary();
      ConnectionDetailsService connDetailsService = (ConnectionDetailsService) beans.getBean(dataSource.getType().getConnDetailsServiceImpl());
      connDetailsService.setConnectionDetails(dataSource, message);
    }

    Map<String, Object> result = createUpdateConfig(document, dataSet);
    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception("Failed to create configuration: " + result.toString());
    }

    String config = (String) ((Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY)).get("_id");
//    roleClient.addIndexToRole(config, request.getRemoteUser() + "_role");

    return result;

  }

  public Map<String, Object> updateConfig(String document, DataSet dataSet) throws Exception {
    dataSet.setId(document);

    validateConfigExists(dataSet, document);

    // set connection details
    for (DataSource dataSource : dataSet.getDataSources()) {
      String message = dataSource.getType() + ". Specimen source? " + dataSource.isPrimary();
      ConnectionDetailsService connDetailsService = (ConnectionDetailsService) beans.getBean(dataSource.getType().getConnDetailsServiceImpl());
      connDetailsService.setConnectionDetails(dataSource, message);
    }

    // add credential from cache if request does not include new credential
    List<DataSource> dataSources = dataSet.getDataSources();
    for (DataSource dataSource : dataSources) {
      // For now we are skipping CSV sources since they are loaded by the user and are stored locally. In the Data Source itself
      if (dataSource.getType() == SourceType.CSV) {
        continue;
      }

      String key = dataSet.getId().concat("-" + dataSource.getSource_id());
      Credential credential = credentialMap.get(key);
      if (dataSource.getConnDetails().getCredential() == null) {
        if (credential != null) {
          dataSource.getConnDetails().setCredential(credential);
        } else {
          throw new Exception(DefaultExceptionHandler.CREDENTIALS_REQUIRED + ": " + dataSet.getName() + "-" + dataSource.getSource_id());
        }
      }
    }

    Map<String, Object> result = createUpdateConfig(document, dataSet);
    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception("Failed to create configuration: " + result.toString());
    }

    for (DataSource dataSource : dataSources) {
      String key = dataSet.getId().concat("-" + dataSource.getSource_id());
      Credential credential = credentialMap.get(key);
      if (dataSource.getConnDetails().getCredential() != null) {
        credentialMap.put(key, credential);
      }
    }

    return result;
  }

  public void validateConfigExists(DataSet dataSet, String document) throws Exception {
    List<String> configs = new LinkedList<String>();
    configs.add(document);
    Map<String, Object> configsResult = getConfigs(configs);

    if ((int) configsResult.get(NetworkClient.COUNT) < 1) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }
  }

  public Map<String, Object> createUpdateConfig(String document, DataSet dataSet) throws Exception {
    Map<String, Object> result = configurationClient.createUpdateConfig(dataSet, document);

    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }

    result.put("doc", document);
    return result;
  }

  public Map<String, Object> deleteConfiguration(String document) throws Exception {
    Map<String, Object> result = configurationClient.deleteConfiguration(document);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    String resultString = (String) responseBody.get(NetworkClient.RESULT);
    if (resultString.equals(NetworkClient.NOT_FOUND)) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }
    return result;
  }

  public Map<String, Object> deleteIntegratedData() {
    return null;
  }

  public Map<String, Object> getIntegrationAndMapping(String documentID, boolean mapping, BasicQuery query, boolean data) throws Exception {
    Map<String, Object> combined = new HashMap<String, Object>();

    if (data) {
      Map<String, Object> integration = this.getIntegration(documentID, query);
      combined.put("integration", integration.get(NetworkClient.RESPONSE_BODY));

      // Extract Count from integration
      logger.info("count");
      logger.info(integration);
      Map<String, Object> integrationResponseBody = (Map<String, Object>) integration.get(NetworkClient.RESPONSE_BODY);
      Map<String, Object> hits = (Map<String, Object>) integrationResponseBody.get("hits");
      combined.put("count", hits.get("total"));
    }

    if (mapping) {
      logger.info("getIntegrationAndMapping");
      Map<String, Object> mappings = this.getMapping(documentID);
      logger.info("getIntegrationAndMapping 1");
      combined.put("mapping", mappings.get(NetworkClient.RESPONSE_BODY));
    }

    Map<String, Object> result = new HashMap<String, Object>();
    result.put(NetworkClient.RESPONSE_BODY, combined);
    result.put(NetworkClient.RESPONSE_CODE, "200");
    return result;
  }

  public Map<String, Object> getDistinctColumnValues(String document, String column) throws Exception {
    Map<String, Object> combined = new HashMap<String, Object>();
    Map<String, Object> result = mappingClient.getDistinctColumnValues(document, column);

    List<Map<String, Object>> responseBody = (List<Map<String, Object>>) result.get(NetworkClient.RESPONSE_BODY);
    return result;
  }

  public Map<String, Object> getIntegration(String documentID, BasicQuery query) throws Exception {
    Map<String, Object> result = integrationClient.getIntegratedData(documentID, query);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
      }
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
    }

    return result;
  }

  public Map<String, Object> getMapping(String documentID) throws Exception {
    List<String> list = new ArrayList<String>();
    list.add(documentID);
    Map<String, Object> result = mappingClient.getMappings(list);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.MAPPING_NOT_FOUND);
      }
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.MAPPING_NOT_FOUND);
    }
    return result;
  }

  public Map<String, Object> getUser(String UserName) throws Exception {
    Map<String, Object> result = roleClient.getUser(UserName);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);

    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.MAPPING_NOT_FOUND);
      }
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.MAPPING_NOT_FOUND);
    }

    return result;
  }

  public Map<String, Object> getBookmarks() throws Exception {
    Map<String, Object> result = bookmarkClient.getBookmarks();
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);

    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.MAPPING_NOT_FOUND);
      }
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }

    return result;
  }

  public Map<String, Object> deleteBookmark(String ID) throws Exception {
    Map<String, Object> result = bookmarkClient.deleteBookmark(ID);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    String resultString = (String) responseBody.get(NetworkClient.RESULT);
    if (resultString.equals(NetworkClient.NOT_FOUND)) {
      throw new Exception(DefaultExceptionHandler.BOOKMARK_NOT_FOUND);
    } else if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.CONFIGURATION_NOT_FOUND);
    }

    return result;
  }

  public Map<String, Object> createBookmark(Bookmark bookmark) throws Exception {
    System.out.println("Creating Bookmark");
    System.out.println(bookmark);
    Map<String, Object> result = bookmarkClient.createBookmark(bookmark);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
      }
      throw new Exception(reason);
    }
    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
    }

    String config = (String) ((Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY)).get("_id");
//    roleClient.addReportToRole(config, request.getRemoteUser() + "_role");

    return result;
  }

  public Map<String, Object> updateBookmark(String ID, Bookmark bookmark) throws Exception {
    System.out.println("Updating Bookmark");
    System.out.println(bookmark);

    Map<String, Object> verify = bookmarkClient.BookmarkExists(ID);
    String resultString = (String) verify.get(NetworkClient.RESPONSE_CODE);
    if (resultString.equals(HttpStatus.NOT_FOUND.toString())) {
      throw new Exception(DefaultExceptionHandler.BOOKMARK_NOT_FOUND);
    } else if (!verify.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !verify.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.BOOKMARK_NOT_FOUND);
    }

    Map<String, Object> result = bookmarkClient.UpdateBookmark(ID, bookmark);
    return result;
  }

  public Map<String, Object> searchAcrossIntegrations(BasicQuery query) throws Exception {

    // Retrieving user's configurations
    Map<String, Object> configResults = configurationClient.getConfigs(null);
    Map<String, Object> responseBody = (Map<String, Object>) configResults.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> hit = (Map<String, Object>) responseBody.get("hits");
    List<Map<String, Object>> configurations = (List<Map<String, Object>>) hit.get("hits");

    // Adding configurations to  Query.
    for (int i = 0; i < configurations.size(); i++) {
      String ID = (String) configurations.get(i).get("_id");
      query.getIndices().add(ID);
    }

    Map<String, Object> result = integrationClient.getMultiIndexIntegratedData(query);

    // Error Handling
    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception("Could not complete Search: " + result.toString());
    }

    return result;
  }

  public Map<String, Object> downloadQuery(String index, BasicQuery query) throws Exception {

    Map<String, Object> verify = networkClient.IndexExists(index);
    String resultString = (String) verify.get(NetworkClient.RESPONSE_CODE);
    if (!verify.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !verify.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.INDEX_NOT_FOUND);
    }

    Map<String, Object> content = new HashMap<String, Object>();
    content.put("responseBody", null);
    content.put("responseCode", "200");

    LinkedList<Object> data = new LinkedList<Object>();

    // Start
    Map<String, Object> scroll = scrollClient.scrollQuery(index, 10000, query);
    String scroll_id = scrollClient.ProccessScroll(scroll, data);

    while (scroll_id != null) {
      Map<String, Object> contScroll = scrollClient.continueScrollQuery(scroll_id);
      scroll_id = scrollClient.ProccessScroll(contScroll, data);
    }

    scrollClient.ProccessScrollingSourceOnly(data);

    System.out.println(data);
    System.out.println(data.size());
    content.put("responseBody", data);

    return content;
  }

  public Map<String, Object> addIndexToRole(String index, String role) throws IOException {
//    return roleClient.addIndexToRole(index, role);
    return null;
  }

  public Map<String, Object> globalSearch(
      String query,
      List<String> indicies,
      int size,
      int from,
      boolean aggregations,
      boolean highlights,
      boolean indexMapping,
      boolean indexNames,
      List<Object> filters,
      String suggestionfield,
      List<String> sources
  ) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(NetworkClient.RESPONSE_BODY, "");
    result.put(NetworkClient.RESPONSE_CODE, "200");

    // If empty grab all integrations user has access to
    if (indicies.size() == 0) {
      indicies.add("j*");
    }

    // Gather Data
    Map<String, Object> searchResult = integrationClient.globalSearch(
        query,
        indicies,
        size,
        from,
        aggregations,
        highlights,
        filters,
        suggestionfield,
        sources
    );
    result.replace(NetworkClient.RESPONSE_BODY, searchResult.get(NetworkClient.RESPONSE_BODY));
    Map<String, Object> body = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);

    // Check Search Results
    if (!searchResult.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !searchResult.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception("Could not complete search: " + searchResult.toString());
    }

    // Get Index mappings
    if (indexMapping) {
      Map<String, Object> mappingsResult = mappingClient.getMappings(indicies);
      body.put("indexmappings", mappingsResult.get(NetworkClient.RESPONSE_BODY));

      // Check Mapping Results
      if (!mappingsResult.get(NetworkClient.RESPONSE_CODE).equals("200")
          && !mappingsResult.get(NetworkClient.RESPONSE_CODE).equals("201")) {
        throw new Exception("Could not complete mapping: " + mappingsResult.toString());
      }
    }

    if (indexNames) {
      // Retrieve Configurations: if it was initially empty we select all
      Map<String, Object> config_results = configurationClient.getConfigs((indicies.get(0) == "j*") ? null : indicies);

      // Check configResults
      if (!config_results.get(NetworkClient.RESPONSE_CODE).equals("200")
          && !config_results.get(NetworkClient.RESPONSE_CODE).equals("201")) {
        throw new Exception("Could not complete configuration mapping: " + config_results.toString());
      }

      // Luke made a good way to enforce this without casting
      Map<String, Object> configResults = (Map<String, Object>) config_results.get(NetworkClient.RESPONSE_BODY);

      Map<String, Object> hits = (Map<String, Object>) configResults.get("hits");
      List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

      // Create Index to Config name Mapping as well
      Map<String, String> IdNameConfigurationNameMapping = new HashMap<String, String>();
      for (Map<String, Object> hit : hitList) {
        String id = (String) hit.get("_id");
        Map<String, Object> source = (Map<String, Object>) hit.get("_source");
        String name = (String) source.get("name");
        IdNameConfigurationNameMapping.put(id, (String) source.get("name"));
      }
      body.put("indexnames", IdNameConfigurationNameMapping);
    }

    return result;
  }
}

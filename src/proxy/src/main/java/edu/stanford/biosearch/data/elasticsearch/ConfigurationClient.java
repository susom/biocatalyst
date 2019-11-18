package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.model.configuration.ConfigsRequest;
import edu.stanford.biosearch.model.configuration.DataSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationClient {
  private static final Logger logger = Logger.getLogger(ConfigurationClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  public Map<String, Object> createUpdateConfig(DataSet dataSet, String document) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat(this.elasticNetwork.configurationIndex).concat(this.elasticNetwork.configurationType).concat("/").concat(document);

    HttpPost post = new HttpPost(url);

    ObjectMapper mapper = new ObjectMapper();
    String dataSetJson = mapper.writeValueAsString(dataSet);

    StringEntity entity = new StringEntity(dataSetJson, ContentType.APPLICATION_JSON);
    post.setEntity(entity);

    Map<String, Object> result = httpExecutor.executeHTTPRequest(post);

    logger.info(result);
    logger.info("FINISH ConfigurationClient.createUpdateConfig");
    return result;
  }

  public Map<String, Object> getConfigs(List<String> configs) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat(this.elasticNetwork.configurationIndex).concat(this.elasticNetwork.elasticsearchIndexSearch);

    Map<String, Object> result = new HashMap<String, Object>();

    HttpPost post = new HttpPost(url);

    ConfigsRequest req = new ConfigsRequest();
    ConfigsRequest.Must must = req.new Must();
    List<Object> musts = new LinkedList<Object>();

    if (configs != null && configs.size() > 0) {
      ConfigsRequest.Match match2 = null;
      ConfigsRequest.Should should = null;
      ConfigsRequest.Bool bool2 = null;
      List<ConfigsRequest.Should> shoulds = new LinkedList<ConfigsRequest.Should>();
      for (String config : configs) {
        match2 = req.new Match();
        match2.getItems().put("_id", config);
        should = req.new Should();
        should.setMatch(match2);
        shoulds.add(should);
        bool2 = req.new Bool();
        bool2.getItems().put("should", shoulds);
      }

      must.getItems().put("bool", bool2);

    }
    Map<String, Object> items = must.getItems();
    for (Map.Entry<String, Object> e : items.entrySet()) {
      musts.add(e);
    }

    ConfigsRequest.Bool bool = req.new Bool();
    bool.getItems().put("must", musts);
    ConfigsRequest.Query query = req.new Query();
    query.setBool(bool);
    req.setQuery(query);
    req.setSize(10000);

    ObjectMapper mapper = new ObjectMapper();
    String reqJson = mapper.writeValueAsString(req);
    logger.info(reqJson);
    StringEntity entity = new StringEntity(reqJson, ContentType.APPLICATION_JSON);
    post.setEntity(entity);

    result = httpExecutor.executeHTTPRequest(post);
    result.put(NetworkClient.COUNT,
        ((Map<String, Object>) ((Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY)).get(NetworkClient.HITS)).get("total"));
    logger.info(result);
    logger.info("FINISH ConfigurationClient.getConfigs");
    return result;
  }

  public Map<String, Object> deleteConfiguration(String document) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat(this.elasticNetwork.configurationIndex).concat(this.elasticNetwork.configurationType).concat("/").concat(document);

    HttpDelete delete = new HttpDelete(url);

    Map<String, Object> result = httpExecutor.executeHTTPRequest(delete);

    logger.info(result);
    logger.info("FINISH ConfigurationClient.createUpdateConfig");
    return result;
  }
}

package edu.stanford.biosearch.data.elasticsearch;

import java.io.IOException;
import java.util.Map;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NetworkClient {
  private static final Logger logger = Logger.getLogger(NetworkClient.class);

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";

  public static final String COUNT = "count";
  public static final String HITS = "hits";
  public static final String SOURCE = "_source";
  public static final String DELETED = "deleted";
  public static final String FOUND = "found";
  public static final String NOT_FOUND = "not_found";
  public static final String NO_SUCH_INDEX = "no such index";
  public static final String REASON = "reason";
  public static final String ERROR = "error";
  public static final String RESULT = "result";

  @Value("${elasticsearch.completion.suggestion.path}")
  public String suggestionPath;

  @Value("${elasticsearch.url}")
  public String elasticsearchUrl;

  @Value("${elasticsearch.config.index}")
  public String configurationIndex;

  @Value("${elasticsearch.config.type}")
  public String configurationType;

  @Value("${elasticsearch.index.search}")
  public String elasticsearchIndexSearch;

  @Value("${elasticsearch.local_storage.index}")
  public String storageIndex = "/localstorage";

  @Value("${elasticsearch.local_storage.type}")
  public String storageType = "/doc";

  @Autowired
  private HttpExecutor httpExecutor;

  // Check if Index Exists
  public Map<String, Object> IndexExists(String index) throws IOException {
    String url = elasticsearchUrl.concat("/").concat(index);
    return this.head(url);
  }

  // Check if Document Exists
  public Map<String, Object> DocumentExists(String index, String mapping, String id) throws IOException {
    String url = elasticsearchUrl.concat("/").concat(index).concat("/").concat(mapping).concat("/").concat(id);
    return this.head(url);
  }

  // Elastic Head
  public Map<String, Object> head(String url) throws IOException {
    HttpHead head = new HttpHead(url);
    return this.httpExecutor.executeHTTPRequest(head);
  }

  // Elastic Get
  public Map<String, Object> get(String url) throws IOException {
    HttpGet get = new HttpGet(url);
    return this.httpExecutor.executeHTTPRequest(get);
  }

  // Elastic Post
  public Map<String, Object> post(String url, String body) throws IOException {
    HttpPost post = new HttpPost(url);

    logger.info(body);

    StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return this.httpExecutor.executeHTTPRequest(post);
  }

  public Map<String, Object> superPost(String url, String body) throws IOException {
    HttpPost post = new HttpPost(url);

    logger.info(body);

    StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return this.httpExecutor.executeHTTPRequestAsAdmin(post);
  }

  // Elastic Delete
  public Map<String, Object> delete(String url) throws IOException {
    HttpDelete delete = new HttpDelete(url);
    return this.httpExecutor.executeHTTPRequest(delete);
  }
}

package edu.stanford.biosearch.data.stride;

import edu.stanford.biosearch.data.elasticsearch.HttpExecutor;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TokenStorageRestClient {

  // Using Elastic as the Token Storage
  // OPTIMIZE: Token Storage Object. Contains Connection Information.
  private HttpExecutor httpExecutor;

  private String elasticsearchUrl;
  private String tokenID = null;
  private String accessToken = null;
  private String refreshToken = null;

  private ElasticTokenIndex index = null;

  public TokenStorageRestClient(HttpExecutor httpExecutor) {
    this.httpExecutor = httpExecutor;
  }

  public void initialize(String ElasticSearchUrl, String ElasticSearchTokenStorageClientIndexID, ElasticTokenIndex Index) {
    this.elasticsearchUrl = ElasticSearchUrl;
    this.tokenID = ElasticSearchTokenStorageClientIndexID;
    this.index = Index;
  }

  public Map<String, Object> getAccessTokens() throws Exception {
    return this.getAccessTokensHelper(this.index.Value());
  }

  private Map<String, Object> getAccessTokensHelper(String type) throws Exception {
    String url = elasticsearchUrl
        .concat("/").concat(type)
        .concat("/").concat("doc")
        .concat("/").concat(tokenID)
        .concat("/").concat("_source");
    return this.get(url);
  }

  public Map<String, Object> setAccessTokens(String accessToken, String refreshToken) throws Exception {
    return this.setAccessTokensHelper(accessToken, refreshToken, this.index.Value());
  }

  private Map<String, Object> setAccessTokensHelper(String accessToken, String refreshToken, String type) throws Exception {
    String url = elasticsearchUrl
        .concat("/").concat(type)
        .concat("/").concat("doc")
        .concat("/").concat(tokenID);

    String backupURL = elasticsearchUrl
        .concat("/").concat("tokens")
        .concat("/").concat("doc");

    Map<String, Object> body = new HashMap<String, Object>();
    // OPTIMIZATION: use the token class instead
    body.put("accessToken", accessToken);
    body.put("refreshToken", refreshToken);

    this.post(url, body);

    // Creating Backup
    Date date = new Date();
    body.put("date", date.getTime());
    return this.post(backupURL, body);
  }

  public String getCachedAccessToken() {
    return this.accessToken;
  }

  public String getRefreshAccessToken() {
    return this.refreshToken;
  }

  // Get
  private Map<String, Object> get(String url) throws IOException {
    HttpGet get = new HttpGet(url);
    Map<String, Object> result = httpExecutor.executeHTTPRequestAsAdmin(get);

    // Caching Access Token
    Map<String, Object> responseBody = (Map<String, Object>) result.get("responseBody");
    this.accessToken = (String) responseBody.get("accessToken");
    this.refreshToken = (String) responseBody.get("refreshToken");

    return result;
  }

  // Post
  private Map<String, Object> post(String url, Map<String, Object> body) throws IOException {
    HttpPost post = new HttpPost(url);
    StringEntity entity = new StringEntity(new JSONObject(body).toString(), ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return httpExecutor.executeHTTPRequestAsAdmin(post);
  }

}

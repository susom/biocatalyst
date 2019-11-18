package edu.stanford.biosearch.service;

import edu.stanford.biosearch.data.elasticsearch.HttpExecutor;
import edu.stanford.biosearch.data.stride.ElasticTokenIndex;
import edu.stanford.biosearch.data.stride.StrideExecutor;
import edu.stanford.biosearch.data.stride.TokenStorageRestClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  @Autowired
  private StrideExecutor strideExecutor;

  @Autowired
  private HttpExecutor elasticExecutor;

  @Value("${stride.api.refresh.url}")
  private String strideRefreshUrl;

  @Value("${elasticsearch.url}")
  private String elasticsearchUrl;

  @Value("${elasticsearch.tokenstoragerestclient.index_id}")
  private String tokenID = null; // OPTIMIZATION: Hard Coded. Move to properties file

  private TokenStorageRestClient strideTokenStorage;
  private TokenStorageRestClient validatorTokenStorage;

  @PostConstruct
  public void init() {
    // Initially Caching Tokens for use.
    System.out.println("Stride Initializing: Starting...");
    try {
      this.strideTokenStorage = new TokenStorageRestClient(this.elasticExecutor);
      this.validatorTokenStorage = new TokenStorageRestClient(this.elasticExecutor);

      this.strideTokenStorage.initialize(this.elasticsearchUrl, this.tokenID, ElasticTokenIndex.Stride);
      this.validatorTokenStorage.initialize(this.elasticsearchUrl, this.tokenID, ElasticTokenIndex.Validator);

      this.getStrideTokens();
      this.getValidatorTokens();
    } catch (Exception e) {
      System.out.println("Stride Initializing: Failed");
    }
    System.out.println("Stride Initializing: Complete");
  }

  @Scheduled(cron = "0 0 0 ? * *") // 12:00 Midnight
  public void reportCurrentTime() {
    System.out.println("Stride Refresh");
    try {
      this.refreshStrideTokens();
      this.refreshValidatorTokens();

      this.getStrideTokens();
      this.getValidatorTokens();
      System.out.println("Stride Refresh:Success");
    } catch (Exception e) {
      System.out.println("Stride Refresh:Failed");
      // Should Send an Email Stating that the refreshing has failed for whatever reason
    }
  }

  public Map<String, Object> getStrideTokens() throws Exception {
    return getTokenHelper(this.strideTokenStorage);
  }

  public Map<String, Object> getValidatorTokens() throws Exception {
    return getTokenHelper(this.validatorTokenStorage);
  }

  private Map<String, Object> getTokenHelper(TokenStorageRestClient tsClient) throws Exception {
    // TODO Auto-generated method stub
    Map<String, Object> result = tsClient.getAccessTokens();
    int responseCode = Integer.parseInt((String) result.get("responseCode"));
    if (responseCode != 200) { // Better Code Response, more Defined. Using Enums
      throw new Exception("Couldn't Retrieve Access Tokens"); // Trigger proper Response
    }
    return result;
  }

  public Map<String, Object> refreshStrideTokens() throws Exception {
    return this.refreshTokens(this.strideTokenStorage);
  }

  public Map<String, Object> refreshValidatorTokens() throws Exception {
    return this.refreshTokens(this.validatorTokenStorage);
  }

  public String getCachedValidatorToken() throws Exception {
    return this.validatorTokenStorage.getCachedAccessToken();
  }

  public String getCachedStrideAccessToken() throws Exception {
    return this.strideTokenStorage.getCachedAccessToken();
  }

  private Map<String, Object> refreshTokens(TokenStorageRestClient tsClient) throws Exception {
    System.out.println("Refreshing Tokens");
    // Get Refresh Token
    String refreshToken = tsClient.getRefreshAccessToken();

    // User Refresh Token
    Map<String, Object> response = null;
    try {
      System.out.println("Refreshing Tokens:" + refreshToken);
      response = refreshAccessTokens(refreshToken);
      System.out.println(response.toString());
      // 401 response is possible
      int responseCode = (int) response.get("responseCode");
      if (responseCode != 200) { // Better Code Response, more Defined. Using Enums
        throw new Exception("Invalid Response"); // Trigger proper Response
      }
    } catch (Exception e) {
      throw new Exception("Couldn't Refresh Tokens");
    }

    try {
      // Response Body is a string.
      String newTokenJSON = (String) response.get("responseBody");
      JSONObject tokens = new JSONObject(newTokenJSON);
      String newAccessToken = (String) tokens.get("accessToken");
      String newRefreshToken = (String) tokens.get("refreshToken");

      // Save Token
      tsClient.setAccessTokens(newAccessToken, newRefreshToken);
    } catch (Exception e) {
      throw new Exception("Couldn't Save Tokens");
    }

    return null;
  }

  // Universal Refresh Command. Becuase Stride/IRB use the same refresh API.
  private Map<String, Object> refreshAccessTokens(String refreshToken) throws Exception {
    Map<String, Object> body = new HashMap<String, Object>();
    body.put("refreshToken", refreshToken);
    return this.post(strideRefreshUrl, body);
  }

  // Post No Authorization needed
  // You are considered trusted because you have the refresh token
  private Map<String, Object> post(String url, Map<String, Object> body) throws IOException {
    HttpPost post = new HttpPost(url);
    StringEntity entity = new StringEntity(new JSONObject(body).toString(), ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return strideExecutor.request(post);
  }
}

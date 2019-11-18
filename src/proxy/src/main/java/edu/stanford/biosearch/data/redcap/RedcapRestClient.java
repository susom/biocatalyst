package edu.stanford.biosearch.data.redcap;

import edu.stanford.biosearch.util.RemoteUser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RedcapRestClient {

  @Autowired
  private RemoteUser request;

  @Value("${redcap.api.url}")
  private String redcapUrl;

  @Value("${redcap.api.projects.url}")
  private String redcapProjectsUrl;

  @Value("${redcap.api.shared.token}")
  private String sharedToken;

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";
  public static final String REQUEST_USER = "requestUser";

  public Map<String, Object> getRedcapProjects() throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("request", "users");
    body.put("user", request.getRemoteUser());
    body.put("token", sharedToken);

    Map<String, Object> result = post(redcapUrl + redcapProjectsUrl, body);
    result.put(REQUEST_USER, request.getRemoteUser());
    return result;
  }

  public Map<String, Object> getRedcapReports(String projectID) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("request", "reports");
    body.put("user", request.getRemoteUser());
    body.put("project_id", projectID);
    body.put("token", sharedToken);

    Map<String, Object> result = post(redcapUrl + redcapProjectsUrl, body);
    return result;
  }

  public Map<String, Object> getRedcapColumns(String projectID, String reportID) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("request", "columns");
    body.put("user", request.getRemoteUser());
    body.put("project_id", projectID);
    body.put("report_id", reportID);
    body.put("token", sharedToken);

    Map<String, Object> result = post(redcapUrl + redcapProjectsUrl, body);
    return result;
  }

  private Map<String, Object> post(String url, Map<String, Object> body) throws IOException {
    HttpPost post = new HttpPost(url);
    StringEntity entity = new StringEntity(new JSONObject(body).toString(), ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return executeHttpRequest(post);
  }

  /**
   * Returns a map with int responseCode and String responseBody. Calling method
   * must deserialize the responseBody into the proper object
   */
  private Map<String, Object> executeHttpRequest(HttpRequestBase request) throws IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(request);

    HttpEntity responseEntity = response.getEntity();
    Map<String, Object> responseData = new HashMap<>();

    if (responseEntity != null) {
      BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntity.getContent()));

      StringBuffer sb = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }

      // ObjectMapper mapper = new ObjectMapper();
      // responseData = mapper.readValue(sb.toString(), new TypeReference<Map<String, Object>>(){});
      responseData.put(RESPONSE_BODY, sb.toString());
    }

    // if(responseData == null) responseData = new HashMap<>();
    responseData.put(RESPONSE_CODE, response.getStatusLine().getStatusCode());

    return responseData;
  }

}

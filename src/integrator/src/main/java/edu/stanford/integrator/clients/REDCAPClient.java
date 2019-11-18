package edu.stanford.integrator.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class REDCAPClient {
  private static final Logger LOGGER = Logger.getLogger(REDCAPClient.class.getName());

  private final List<NameValuePair> basicParams;

  public REDCAPClient() {
    basicParams = new ArrayList<>();
    basicParams.add(new BasicNameValuePair("format", "json"));
    basicParams.add(new BasicNameValuePair("rawOrLabel", "label"));
    basicParams.add(new BasicNameValuePair("rawOrLabelHeaders", "label"));
    basicParams.add(new BasicNameValuePair("exportCheckboxLabel", "true"));
    basicParams.add(new BasicNameValuePair("returnFormat", "json"));
  }

  public String getColumnJSON(
      String redcapApiUrl,
      String reportId,
      String projectId,
      String remoteUser,
      String sharedKey) throws Exception {
    return getJsonData(redcapApiUrl, "columns", reportId, projectId, remoteUser, sharedKey);
  }

  public String getReportJSON(
      String redcapApiUrl,
      String reportId,
      String projectId,
      String remoteUser,
      String sharedKey) throws Exception {
    return getJsonData(redcapApiUrl, "reports", reportId, projectId, remoteUser, sharedKey);
  }

  public String getJsonData(
      String redcapApiUrl,
      String request,
      String reportId,
      String projectId,
      String remoteUser,
      String sharedKey) throws Exception {
    HttpPost post = generatePost(redcapApiUrl, request, reportId, projectId, remoteUser, sharedKey);
    LOGGER.log(Level.INFO, "Pulling REDCap report " + reportId + " for user " + remoteUser + ", project_id: " + projectId);
    HttpResponse resp = this.post(post);
    return convertToJSONString(resp, request.toUpperCase() + reportId);
  }

  private HttpPost generatePost(
      String redcapApiUrl,
      String request,
      String reportId,
      String projectId,
      String remoteUser,
      String sharedKey) throws Exception {

    if (reportId == null || reportId.equals("") || projectId == null || projectId.equals("") || redcapApiUrl == null
        || redcapApiUrl.equals("")) {
      throw new IllegalArgumentException("REDCapClient: invalid connection information");
    }

    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> body = new HashMap<String, String>();
    body.put("token", sharedKey);
    body.put("request", request);
    body.put("project_id", projectId);
    body.put("report_id", reportId);
    body.put("user", remoteUser);
    String jsonBody = mapper.writeValueAsString(body);

    StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);

    URIBuilder uriBuilder = new URIBuilder(redcapApiUrl);
    HttpPost post = new HttpPost(uriBuilder.build());
    post.setEntity(entity);

    return post;
  }

  private String convertToJSONString(HttpResponse resp, String jsonObjectName) {
    String result = "";
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
      result += "{ \"" + jsonObjectName + "\": ";
      String line;
      while ((line = reader.readLine()) != null) {
        result += line;
      }
      result += "}";
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Unable to read HTTP response content", ex);
      throw new RuntimeException("Unable to read HTTP response content");
    }

    return result;
  }

  private HttpResponse post(HttpPost post) throws Exception {
    final HttpClient client = HttpClientBuilder.create().build();
    HttpResponse resp = client.execute(post);

    int respCode = resp.getStatusLine().getStatusCode();
    if (respCode != 200) {

      String reason = "";
      try {
        reason += ": " + resp.getStatusLine().getReasonPhrase();
      } catch (Exception e) {
        // swallow the Exception. It's ok if we don't report the reason
      }
      HttpEntity entity = resp.getEntity();
      String responseString = EntityUtils.toString(entity, "UTF-8");
      throw new Exception("REDCap server response: " + respCode + reason + " " + responseString);
    }

    return resp;
  }

}

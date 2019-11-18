package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class HttpExecutor {

  private static final Logger logger = Logger.getLogger(HttpExecutor.class);

  /**
   * Executes an HTTP request. The aspect in ElasticsearchAspect.java adds the user's credentials.
   *
   * @param request
   * @return
   * @throws IOException
   */
  public Map<String, Object> executeHTTPRequest(HttpRequestBase request) throws IOException {
    request.setHeader("Content-Type", "application/json");

    Map<String, Object> result = new HashMap<String, Object>();
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(request);

    int responseCode = response.getStatusLine().getStatusCode();
    logger.info(responseCode);
    result.put(NetworkClient.RESPONSE_CODE, String.valueOf(responseCode));

    HttpEntity responseEntity = response.getEntity();
    result.put(NetworkClient.RESPONSE_BODY, ""); // Default Empty
    if (responseEntity != null) {
      BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntity.getContent()));

      StringBuffer sb = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> resp = (Map<String, Object>) mapper.readValue(sb.toString(), Object.class);
      result.put(NetworkClient.RESPONSE_BODY, resp);
    }

    logger.info(result);
    logger.info("FINISH HttpExecutor.executeHTTPRequest");
    return result;
  }

  /**
   * Executes an HTTP request. The aspect in ElasticsearchAspect.java adds superuser credentials.
   * Since executeHTTPRequest is called from a method in the same class, its aspect is not called.
   *
   * @param request
   * @return
   * @throws IOException
   */
  public Map<String, Object> executeHTTPRequestAsAdmin(HttpRequestBase request) throws IOException {
    return executeHTTPRequest(request);
  }

}

package edu.stanford.biosearch.data.stride;

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
public class StrideExecutor {
  private static final Logger logger = Logger.getLogger(StrideExecutor.class);

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";
  public static final String REQUEST_USER = "requestUser";

  public Map<String, Object> request(HttpRequestBase request) throws IOException {
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

      responseData.put(RESPONSE_BODY, sb.toString());
    }
    responseData.put(RESPONSE_CODE, response.getStatusLine().getStatusCode());

    return responseData;
  }

  public Map<String, Object> authorizedRequest(HttpRequestBase request) throws IOException {
    return this.request(request);
  }

  public Map<String, Object> authorizedValidatorRequest(HttpRequestBase request) throws IOException {
    return this.request(request);
  }

}

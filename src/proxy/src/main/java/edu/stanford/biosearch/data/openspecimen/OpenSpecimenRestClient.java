package edu.stanford.biosearch.data.openspecimen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class OpenSpecimenRestClient {
  private static final Logger logger = Logger.getLogger(OpenSpecimenRestClient.class);

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";

  public Map<String, Object> getCollectionProtocols(String credential, String url, String remoteUser) throws IOException {
    Map<String, Object> result = new HashMap<String, Object>();
    HttpGet get = new HttpGet(url);

    get.setHeader(HttpHeaders.AUTHORIZATION, credential);
    get.setHeader("X-LOGIN-NAME-ACCESS", remoteUser);
    get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    HttpClient httpClient = HttpClientBuilder.create().build();

    HttpResponse response = httpClient.execute(get);
    int responseCode = response.getStatusLine().getStatusCode();
    result.put(RESPONSE_CODE, String.valueOf(responseCode));

    HttpEntity responseEntity = response.getEntity();
    BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntity.getContent()));

    StringBuffer sb = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {
      sb.append(line);
    }
    result.put(RESPONSE_BODY, sb.toString());

    logger.info(result);
    logger.info("FINISH OpenSpecimenRestClient.getCollectionProtocols");
    return result;
  }
}

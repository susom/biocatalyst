package edu.stanford.integrator.clients;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IndexerRestClient {

  @Value("${indexer.url}")
  public String indexer_url;

  private static final Logger LOGGER = Logger.getLogger(IndexerRestClient.class.getName());
  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";

  public Map<String, Object> index(String selectStatement, String indexName, String pipelineId, boolean cleanIndex) throws ClientProtocolException, IOException, URISyntaxException {
    LOGGER.log(Level.INFO, "Connecting to indexer using indexer.url: " + indexer_url);

    Map<String, Object> result = new HashMap<String,Object>();

    String contentString = "{\"sql\" : \"" + selectStatement+ "\","
            + "\"indexName\":\"" + indexName + "\","
            + "\"pipelineId\":\"" + pipelineId + "\""
            + "}";
    LOGGER.log(Level.INFO, "Content String: " + contentString);
    StringEntity stringEntity = new StringEntity(contentString);

    URIBuilder builder = new URIBuilder(indexer_url);
    builder.setParameter("clean", String.valueOf(cleanIndex));
    HttpPost post = new HttpPost(builder.build());
    post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    post.setEntity(stringEntity);

    HttpClient httpClient = HttpClientBuilder.create().build();

    HttpResponse response = httpClient.execute(post);
    int responseCode = response.getStatusLine().getStatusCode();
    result.put(RESPONSE_CODE, String.valueOf(responseCode));

    HttpEntity responseEntity = response.getEntity();
    BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntity.getContent()));

    StringBuffer sb = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {
      sb.append(line);
    }
    result.put(RESPONSE_BODY, sb.toString() );

    return result;
  }

  public Map<String, Object> getIndex(String id) throws URISyntaxException, ClientProtocolException, IOException  {
    Map<String, Object> result = new HashMap<String,Object>();

    URIBuilder builder = new URIBuilder(indexer_url);
    builder.setParameter("id", id);
    HttpGet get = new HttpGet(builder.build());

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
    result.put(RESPONSE_BODY, sb.toString() );

    return result;
  }

}
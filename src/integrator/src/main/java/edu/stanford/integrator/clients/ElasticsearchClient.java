package edu.stanford.integrator.clients;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;

public class ElasticsearchClient extends RestClient {
  private String credentials;

  public ElasticsearchClient(String elasticUrl, String credentials) {
    super(elasticUrl);
    this.credentials = credentials;
  }

  @Override
  public String get() throws IOException {
    get.setHeader("Authorization", credentials);
    return super.get();
  }

  @Override
  public CloseableHttpResponse post(JsonNode json) throws Exception {
    get.setHeader("Authorization", credentials);
    return super.post(json);
  }

  @Override
  public CloseableHttpResponse post(StringEntity stringEntity) throws Exception {
    get.setHeader("Authorization", credentials);
    return super.post(stringEntity);
  }

  @Override
  public int put(JsonNode json) throws Exception {
    get.setHeader("Authorization", credentials);
    return super.put(json);
  }

  @Override
  public int put(StringEntity stringEntity) throws Exception {
    get.setHeader("Authorization", credentials);
    return super.put(stringEntity);
  }

}

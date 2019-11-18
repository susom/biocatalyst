package edu.stanford.integrator.clients;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;

public class OpenSpecimenClient extends RestClient {
  private static final Logger LOGGER = Logger.getLogger(OpenSpecimenClient.class.getName());
  private final String credentials;

  public OpenSpecimenClient(String url, String credentials) {
    super(url);
    this.credentials = credentials;
  }

  public String get() throws IOException {
    this.get.addHeader("User-Agent", HttpHeaders.USER_AGENT);
    return super.get();
  }

  public CloseableHttpResponse post(JsonNode JSON) throws Exception {
    post.setHeader("Authorization", this.credentials);
    return super.post(JSON);
  }
}
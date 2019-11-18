package edu.stanford.integrator.clients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.util.Http;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public abstract class RestClient {
  private static final Logger LOGGER = Logger.getLogger(RestClient.class.getName());

  protected HttpPost post;
  protected HttpGet get;
  protected HttpPut put;
  private CloseableHttpClient client;

  public RestClient(String url) {
    if (url == null || url.equals("")) {
      throw new IllegalArgumentException(url);
    }

    this.post = new HttpPost(url);
    this.get = new HttpGet(url);
    this.put = new HttpPut(url);

    // TODO: when in production - remove this, to only accept official certificates
    ////////////  temporarily - accepting self signed certificates:  ///////////// remove this section when using valid certificates
    try {
      SSLContextBuilder builder = new SSLContextBuilder();
      builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
      client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error preparing to connect to REST server", ex);
      return;
    }
    /////////// uncomment the following when when using only valid certificates (not self signed) //////////////////////////////
    //client = HttpClientBuilder.create().build();
  }

  public String get() throws IOException {
    CloseableHttpResponse response = client.execute(get);
    LOGGER.log(Level.INFO, "Response Code : " + response.getStatusLine().getStatusCode());
    return Http.responseToString(response);
  }

  public CloseableHttpResponse post(JsonNode json) throws Exception {
    LOGGER.log(Level.INFO, "Post JSON : " + new ObjectMapper().writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(json));

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);

    return this.post(new StringEntity(mapper.writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(json)));
  }

  public CloseableHttpResponse post(StringEntity stringEntity) throws Exception {
    stringEntity.setContentType("application/json");

    RequestConfig.Builder requestConfig = RequestConfig.custom();
    requestConfig.setConnectTimeout(60 * 1000);
    requestConfig.setConnectionRequestTimeout(60 * 1000);
    requestConfig.setSocketTimeout(60 * 1000);
    post.setConfig(requestConfig.build());

    post.setHeader("content-type", "application/json");
    post.setEntity(stringEntity);

    CloseableHttpResponse response = client.execute(post);

    if (response == null) {
      throw new Exception("No response from server");
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new Exception("server response: " + statusCode);
    }

    return response;
  }

  public int put(JsonNode json) throws Exception {
    LOGGER.log(Level.INFO, "Put JSON : " + new ObjectMapper().writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(json));

    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);

    return this.put(new StringEntity(mapper.writerWithDefaultPrettyPrinter().withDefaultPrettyPrinter().writeValueAsString(json)));
  }

  public int put(StringEntity stringEntity) throws Exception {
    stringEntity.setContentType("application/json");

    put.setHeader("content-type", "application/json");
    put.setEntity(stringEntity);

    CloseableHttpResponse response = client.execute(put);

    if (response == null) {
      throw new Exception("No response from server");
    }

    int respCode = response.getStatusLine().getStatusCode();
    if (respCode != 200) {
      throw new Exception("server response: " + respCode);
    }

    return respCode;
  }
}

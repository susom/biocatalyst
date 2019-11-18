package edu.stanford.integrator.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.stanford.integrator.util.Base64Util;
import edu.stanford.integrator.util.Http;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

@Service
public class LocalStorageClient {
  private static final Logger LOGGER = Logger.getLogger(LocalStorageClient.class.getName());

  @Value("${elasticsearch.local_storage.localStorage}")
  public String elasticLocalStorageUrl;

  @Value("${elasticsearch.local_storage.preview}")
  public String elasticLocalStoragePreviewUrl;

  @Value("${elasticsearch.local_storage.document_type}")
  public String storageDocumentType;

  @Value("${elasticsearch.password}")
  public String password;

  @Autowired
  Base64Util base64Util;

  public JsonNode getOne(String ID, String remoteUser) throws Exception {
    ArrayNode data = this.get(ID, remoteUser);
    return data.get(0);
  }

  public JsonNode getData(String ID, String remoteUser) throws Exception {
    return this.get(ID, remoteUser);
  }

  private ArrayNode get(String ID, String remoteUser) throws Exception {
    String url = this.elasticLocalStorageUrl
        .concat(storageDocumentType)
        .concat("/").concat(ID)
        .concat("/").concat("_source");

    HttpGet get = new HttpGet(url);
    LOGGER.info("Accessing Local Storage for remote user: " + remoteUser);
    String credential = base64Util.encodeCredentials(remoteUser, password);
    get.addHeader(HttpHeaders.AUTHORIZATION, credential);

    HttpResponse response = this.execute(get);
    String content = this.extractContent(response);
    JsonNode json = this.convertStringJsonToJsonNode(content);
    ArrayNode result = this.extractDataFromElasticResponse(json);
    return result;
  }

  private HttpResponse execute(HttpUriRequest request) throws Exception {
    final HttpClient client = HttpClientBuilder.create().build();

    HttpResponse resp = client.execute(request);

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
      throw new Exception("Elastic server response: " + respCode + reason + " " + responseString);
    }

    return resp;
  }

  private String extractContent(HttpResponse resp) {
    return Http.responseToString(resp);
  }

  private JsonNode convertStringJsonToJsonNode(String json) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, JsonNode.class);
  }

  private ArrayNode extractDataFromElasticResponse(JsonNode elasticResponse) {
    JsonNode data = elasticResponse.get("data");
    return (ArrayNode) data.get("rows");
  }
}

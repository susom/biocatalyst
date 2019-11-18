package edu.stanford.biosearch.data.stride;

import edu.stanford.biosearch.service.TokenService;
import edu.stanford.biosearch.util.RemoteUser;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableScheduling
public class StrideRestClient {
  @Value("${stride.api.irbs.protocol.url}")
  private String irbProtocolUrl;

  @Value("${stride.api.irbs.all.url}")
  private String irbAllUrl;

  @Value("${stride.api.irbs.valid.url}")
  private String irbValidUrl;

  @Value("${stride.api.metadata.url}")
  private String strideMetadataUrl;

  @Value("${stride.api.request.url}")
  private String strideRequestUrl;

  @Value("${stride.api.status.url}")
  private String strideStatusUrl;

  @Value("${stride.api.retrieve.url}")
  private String strideRetrieveUrl;

  @Autowired
  RemoteUser remoteUser;

  @Autowired
  StrideExecutor strideExecutor;

  @Autowired
  TokenService tokenService;

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";
  public static final String REQUEST_USER = "requestUser";

  public Map<String, Object> getIRBs() throws IOException {
    JSONArray validProtocols = new JSONArray();

    StringBuilder url = new StringBuilder(this.irbAllUrl);
    url.append(this.remoteUser.getRemoteUser());

    HttpGet get = new HttpGet(url.toString());
    JSONObject returnValue = new JSONObject(strideExecutor.authorizedValidatorRequest(get));
    JSONObject responseBody = new JSONObject((String) returnValue.get("responseBody"));
    JSONArray protocols = (JSONArray) responseBody.get("protocols");
    for (int index = 0; index < protocols.length(); index++) {
      JSONObject protocol = protocols.getJSONObject(index);

      Boolean valid = protocol.getBoolean("isValid");
      String protocolState = protocol.getString("protocolState");
      String expireyDate = protocol.getString("expireDate");
      SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
      Date expireDate = null;

      try {
        expireDate = sdf.parse(expireyDate);
        if (valid) {
          if (protocolState.equals("APPROVED")) {
            if (expireDate.after(new Date())) {
              validProtocols.put(protocol);
            }
          }
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    return returnValue.toMap();
  }

  public Map<String, Object> getMetaData(int IRBNumber) throws IOException {
    Map<String, Object> body = new HashMap<>();
    body.put("universityId", this.remoteUser.getRemoteUser());
    body.put("irb_number", IRBNumber);
    return this.post(this.strideMetadataUrl, body);
  }

  private Map<String, Object> post(String url, Map<String, Object> body) throws IOException {
    HttpPost post = new HttpPost(url);
    StringEntity entity = new StringEntity(new JSONObject(body).toString(), ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    return authorizedHTTPRequest(post);
  }

  private Map<String, Object> authorizedHTTPRequest(HttpRequestBase request) throws IOException {
    System.out.println("authorizedHTTPRequest");
    return strideExecutor.authorizedRequest(request);
  }
}

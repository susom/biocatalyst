package edu.stanford.biosearch.data.openspecimen;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class TestRestClient {
  private static final Logger logger = Logger.getLogger(TestRestClient.class);

  public static final String RESPONSE_CODE = "responseCode";
  public static final String RESPONSE_BODY = "responseBody";

  public static Map<String, Object> getTest(String url) throws IOException {

    Map<String, Object> result = new HashMap<String, Object>();

    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
          throws CertificateException {
        // TODO Auto-generated method stub

      }

      @Override
      public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
          throws CertificateException {
        // TODO Auto-generated method stub

      }
    }
    };

    // Install the all-trusting trust manager
    SSLContext sc;
    try {
      sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (KeyManagementException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Create all-trusting host name verifier
    HostnameVerifier allHostsValid = new HostnameVerifier() {
      @Override
      public boolean verify(String arg0, SSLSession arg1) {
        // TODO Auto-generated method stub
        return false;
      }
    };

    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    //HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(url);

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

    return result;
  }

  public static void main(String[] args) throws IOException {
    String url = "http://es:9200";

    System.out.println(TestRestClient.getTest(url));
  }
}

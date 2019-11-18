package edu.stanford.biosearch.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.data.elasticsearch.NetworkClient;
import edu.stanford.biosearch.data.elasticsearch.RoleClient;
import edu.stanford.biosearch.util.Base64Util;
import edu.stanford.biosearch.util.RemoteUser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ElasticsearchAspect {
  private static final Logger logger = Logger.getLogger(ElasticsearchAspect.class);

  @Value("${elasticsearch.password}")
  private String password;

  @Value("${elasticsearch.admin.user}")
  private String adminUsername;

  @Value("${elasticsearch.admin.password}")
  private String adminPassword;

  @Value("${elasticsearch.url}")
  private String elasticUrl;

  @Autowired
  private Base64Util base64Util;

  @Autowired
  RemoteUser remoteUser;

  @Autowired
  NetworkClient networkClient;

  @Autowired
  RoleClient roleClient;

  /**
   * Executes an HTTP request with the user's credentials
   *
   * @param pjp
   * @return
   * @throws Throwable
   */
  @Around("execution(* edu.stanford.biosearch.data.elasticsearch.HttpExecutor.executeHTTPRequest(..))")
  public Object elasticsearchWithUser(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("START ElasticsearchAspect.elasticsearchWithUser");

    String credential = base64Util.encode(remoteUser.getRemoteUser(), password);

    HttpRequestBase base = (HttpRequestBase) pjp.getArgs()[0];
    base.setHeader(HttpHeaders.AUTHORIZATION, credential);

    Map<String, Object> result = null;
    try {
      result = (Map<String, Object>) pjp.proceed();
      String responseCode = (String) result.get(NetworkClient.RESPONSE_CODE);
      if (responseCode.equals(HttpStatus.UNAUTHORIZED.toString())) {
        createUserGroup();
        createUser();
        createSamlUserMapping();
        result = (Map<String, Object>) pjp.proceed();
      }
    } catch (Throwable e) {
      logger.error("ERROR in ElasticsearchAspect.elasticsearchWithUser " + e.getMessage());
      throw e;
    }

    logger.info("FINISH ElasticsearchAspect.elasticsearchWithUser");

    return result;
  }

  /**
   * Executes an HTTP request using the superuser credentals
   *
   * @param pjp
   * @return
   * @throws Throwable
   */
  @Around("execution(* edu.stanford.biosearch.data.elasticsearch.HttpExecutor.executeHTTPRequestAsAdmin(..))")
  public Object elasticsearchWithAdmin(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("START ElasticsearchAspect.elasticsearchWithAdmin");

    String credential = base64Util.encode(adminUsername, adminPassword);

    HttpRequestBase base = (HttpRequestBase) pjp.getArgs()[0];
    base.setHeader(HttpHeaders.AUTHORIZATION, credential);

    Map<String, Object> result = null;
    try {
      result = (Map<String, Object>) pjp.proceed();
    } catch (Throwable e) {
      logger.error("ERROR in ElasticsearchAspect.elasticsearchWithAdmin " + e.getMessage());
      throw e;
    }

    logger.info("FINISH ElasticsearchAspect.elasticsearchWithAdmin");

    return result;
  }

  /**
   * Creates a custom role for a user. The role named "username_role"
   *
   * @return Status Code for creating the role
   * @throws IOException Http or JSON error
   */
  private int createUserGroup() throws IOException {
//    HttpPost post = new HttpPost(elasticUrl + "/_xpack/security/role/" + remoteUser.getRemoteUser() + "_role");
//
//    Map<String, Object> body = new HashMap<>();
//    body.put("cluster", new String[] {});
//    Map<String, Object> integrationsPrivilege = roleClient.generateBasicRolePermission("integrations",
//        new String[] {"read", "write",});
//
//    // Generated integration indices
//    // Abusing first index to give a name for lookup later when modifying. Unable to name an index privelege in elastic.
//    Map<String, Object> indexPrivilege = roleClient.generateBasicRolePermission("__integrations__",
//        new String[] {"read", "write", "manage"}, false);
//
//    Map<String, Object> bookmarksPrivilege = roleClient.generateBasicRolePermission("reports",
//        new String[] {"read", "write"});
//
//    Map<String, Object> storagePrivilege = roleClient.generateBasicRolePermission("localstorage",
//        new String[] {"read", "write"});
//
//    body.put("indices", new Map[] {integrationsPrivilege, indexPrivilege, bookmarksPrivilege, storagePrivilege});
//    String json = new ObjectMapper().writeValueAsString(body);
//    post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
//
//    return superUserRequest(post);
    return 0;
  }

  /**
   * Creates a new user in Elasticsearch
   *
   * @return Status Code for adding the new user
   * @throws IOException Http or JSON error
   */
  private int createUser() throws IOException {
//    HttpPost post = new HttpPost(elasticUrl + "/_xpack/security/user/" + remoteUser.getRemoteUser());
//
//    Map<String, Object> body = new HashMap<>();
//    body.put("password", password);
//    body.put("roles", new String[] {remoteUser.getRemoteUser() + "_role"}); // each user's role is "username_role"
//    body.put("full_name", remoteUser.getDisplayName());
//    body.put("email", remoteUser.getMail());
//    String json = new ObjectMapper().writeValueAsString(body);
//    post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
//
//    return superUserRequest(post);
    return 0;
  }

  /**
   * SAML user-role mapping
   *
   * @return
   * @throws IOException
   */
  private int createSamlUserMapping() throws IOException {
//    HttpPut put = new HttpPut(elasticUrl + "/_xpack/security/role_mapping/" + remoteUser.getRemoteUser());
//
//    Map<String, Object> body = new HashMap<>();
//    body.put("roles", new String[] {remoteUser.getRemoteUser() + "_role", "kibana_user"});
//    body.put("enabled", true);
//    Map<String, Object> all = new HashMap<>();
//    Map<String, Object> field1 = new HashMap<>();
//    Map<String, String> realm = new HashMap<>();
//    realm.put("realm.name", "stanfordsaml");
//    field1.put("field", realm);
//    Map<String, Object> field2 = new HashMap<>();
//    Map<String, String> username = new HashMap<>();
//    username.put("username", remoteUser.getRemoteUser());
//    field2.put("field", username);
//    all.put("all", new Map[] {field1, field2});
//    body.put("rules", all);
//    String json = new ObjectMapper().writeValueAsString(body);
//    put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
//
//    return superUserRequest(put);
    return 0;
  }

  /**
   * Submits a http request with superuser creds
   *
   * @param request request to send
   * @return status code of request
   * @throws IOException an error occurs
   */
  private int superUserRequest(HttpRequestBase request) throws IOException {
    String credential = base64Util.encode(adminUsername, adminPassword);
    request.setHeader(HttpHeaders.AUTHORIZATION, credential);
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(request);

    return response.getStatusLine().getStatusCode();
  }
}

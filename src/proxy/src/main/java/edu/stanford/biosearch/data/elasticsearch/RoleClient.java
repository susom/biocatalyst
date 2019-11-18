package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.model.integrateddata.ElasticQuery;
import edu.stanford.biosearch.util.RemoteUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleClient {
  private static final Logger logger = Logger.getLogger(RoleClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  @Autowired
  RemoteUser remoteUser;

  // Elastic User
  public Map<String, Object> getUser(String UserID) throws IOException {
    String elasticUrl = this.elasticNetwork.elasticsearchUrl.concat("/").concat("users").concat("/")
        .concat("_search");

    Map<String, Object> filters = new HashMap<>();
    Map<String, Object> match = new HashMap<>();
    match.put("Name", UserID);
    filters.put("match", match);

    ElasticQuery query = new ElasticQuery(new LinkedList<>(), filters, new HashMap<>(), 0, 10);

    return this.elasticNetwork.post(elasticUrl, query.jsonStringify());
  }

  private Map<String, Object> getRole(String role) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl + "/_xpack/security/role/" + role;
    return httpExecutor.executeHTTPRequestAsAdmin(new HttpGet(url));
  }

  public Map<String, Object> addIndexToRole(String index, String role) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl + "/_xpack/security/role/" + role;

    // need to get the data to modify
    Map<String, Object> roleData = httpExecutor.executeHTTPRequestAsAdmin(new HttpGet(url));

    // have to get into the right data to add the index name
    Map<String, Object> responseBody = (Map<String, Object>) roleData.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> roleBody = (Map<String, Object>) responseBody.get(role); // this is the body to submit
    ArrayList<Map<String, Object>> indices = (ArrayList<Map<String, Object>>) roleBody.get("indices");
    Map<String, Object> configRuleData = indices.stream().filter(i -> ((ArrayList<String>) i.get("names")).get(0)
        .equals(this.elasticNetwork.configurationIndex.substring(1))).findFirst().get();
    Map<String, Object> ruleData = indices.stream().filter(i -> !((ArrayList<String>) i.get("names")).get(0)
        .equals(this.elasticNetwork.configurationIndex.substring(1))).findFirst().get();
    ArrayList<String> names = (ArrayList<String>) ruleData.get("names");
    names.add(index);
    String termsQuery = (String) configRuleData.get("query");
    configRuleData.put("query", termsQuery.substring(0, termsQuery.length() - 3) + ",\"" + index + "\"]}}");

    return this.UpdateRole(role, roleBody);
  }

  public Map<String, Object> addReportToRole(String index, String role) throws IOException {
    // need to get the data to modify
    Map<String, Object> roleData = this.getRole(role);

    // have to get into the right data to add the index name
    Map<String, Object> responseBody = (Map<String, Object>) roleData.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> roleBody = (Map<String, Object>) responseBody.get(role); // this is the body to submit
    ArrayList<Map<String, Object>> indices = (ArrayList<Map<String, Object>>) roleBody.get("indices");
    Map<String, Object> configRuleData = indices.stream()
        .filter(i -> ((ArrayList<String>) i.get("names")).get(0).equals("reports")).findFirst().get();

    if (!configRuleData.containsKey("query")) {
      configRuleData.put("query", "{\"terms\":{\"_id\":[]}}");
      String termsQuery = (String) configRuleData.get("query");
      configRuleData.put("query", termsQuery.substring(0, termsQuery.length() - 3) + "\"" + index + "\"]}}");
    } else {
      String termsQuery = (String) configRuleData.get("query");
      configRuleData.put("query", termsQuery.substring(0, termsQuery.length() - 3) + ",\"" + index + "\"]}}");
    }

    return this.UpdateRole(role, roleBody);
  }

  public Map<String, Object> addLocalStorageToRole(String index) throws IOException {
    return this.addLocalStorageToRole(index, remoteUser.getRemoteUser() + "_role");
  }

  private Map<String, Object> addLocalStorageToRole(String index, String role) throws IOException {
    // need to get the data to modify
    Map<String, Object> roleData = getRole(role);

    // have to get into the right data to add the index name
    Map<String, Object> responseBody = (Map<String, Object>) roleData.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> roleBody = (Map<String, Object>) responseBody.get(role); // this is the body to submit
    ArrayList<Map<String, Object>> indices = (ArrayList<Map<String, Object>>) roleBody.get("indices");

    Map<String, Object> configRuleData = indices.stream()
        .filter(i -> ((ArrayList<String>) i.get("names")).get(0).equals("localstorage")).findFirst().get();

    if (configRuleData == null) {
      configRuleData = generateBasicRolePermission("localstorage");
    }

    String termsQuery = (String) configRuleData.get("query");
    configRuleData.put("query", termsQuery.substring(0, termsQuery.length() - 3) + ",\"" + index + "\"]}}");

    return this.UpdateRole(role, roleBody);
  }

  private Map<String, Object> UpdateRole(String role, Map<String, Object> roleBody) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl + "/_xpack/security/role/" + role;
    HttpPut put = new HttpPut(url);
    String json = new ObjectMapper().writeValueAsString(roleBody);
    put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    return httpExecutor.executeHTTPRequestAsAdmin(put);
  }

  private Map<String, Object> generateBasicRolePermission(String Name) {
    return this.generateBasicRolePermission(Name, new String[] {"read", "write", "manage"}, true);
  }

  public Map<String, Object> generateBasicRolePermission(String Name, String[] privileges) {
    return this.generateBasicRolePermission(Name, privileges, true);
  }

  public Map<String, Object> generateBasicRolePermission(String Name, String[] privileges, boolean enableQuery) {
    Map<String, Object> permission = new HashMap<>();
    permission.put("names", new String[] {Name});
    permission.put("privileges", privileges);
    Map<String, Object> storagePrivilegeFieldSec = new HashMap<>();
    storagePrivilegeFieldSec.put("grant", new String[] {"*"});
    permission.put("field_security", storagePrivilegeFieldSec);
    if (enableQuery) {
      permission.put("query", "{\"terms\":{\"_id\":[\"null\"]}}");
    }
    return permission;
  }

}

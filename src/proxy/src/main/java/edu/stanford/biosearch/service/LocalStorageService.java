package edu.stanford.biosearch.service;

import edu.stanford.biosearch.config.DefaultExceptionHandler;
import edu.stanford.biosearch.data.elasticsearch.NetworkClient;
import edu.stanford.biosearch.data.elasticsearch.RoleClient;
import edu.stanford.biosearch.data.elasticsearch.StorageClient;
import edu.stanford.biosearch.model.localstorage.StorageObject;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LocalStorageService {
  @Autowired
  StorageClient elasticStorageClient;

  @Autowired
  private NetworkClient elasticNetwork;

  @Autowired
  private RoleClient roleClient;

  public Map<String, Object> Create(StorageObject obj) throws Exception {
    Map<String, Object> result = this.elasticStorageClient.Create(obj);
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
      }
      throw new Exception(reason);
    }

    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")
        && !result.get(NetworkClient.RESPONSE_CODE).equals("201")) {
      throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
    }

    String config = (String) ((Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY)).get("_id");
//    roleClient.addLocalStorageToRole(config);

    return result;

  }

  public Map<String, Object> Get(String ID) throws Exception {
    return this.elasticStorageClient.Get(ID);
  }

  public Map<String, Object> Preview(String ID) throws Exception {
    Map<String, Object> result = this.elasticStorageClient.Get(ID);

    // Grab Data Field
    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

    // CSV Specific Optimization
    try {
      List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
      if (rows != null) {
        int length = rows.size();
        data.put("rows", rows.subList(0, Math.min(10, length)));
      }
    } catch (Exception e) {
      // Swallowing exception for now and just returning all the results.
    }

    return result;
  }

  public Map<String, Object> Update(StorageObject obj, String ID) throws Exception {
    Map<String, Object> result = this.elasticStorageClient.Update(obj, ID);

    Map<String, Object> responseBody = (Map<String, Object>) result.get(NetworkClient.RESPONSE_BODY);
    if (responseBody.containsKey("error")) {
      Map<String, Object> error = (Map<String, Object>) responseBody.get(NetworkClient.ERROR);
      String reason = (String) error.get(NetworkClient.REASON);
      if (reason.equals(NetworkClient.NO_SUCH_INDEX)) {
        throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
      }
      throw new Exception(reason);
    }

    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")) {
      throw new Exception(DefaultExceptionHandler.INTEGRATION_NOT_FOUND);
    }
    return result;
  }

  public Map<String, Object> Delete(String ID) throws Exception {
    Map<String, Object> result = this.elasticStorageClient.Delete(ID);

    if (!result.get(NetworkClient.RESPONSE_CODE).equals("200")) {
      throw new Exception(DefaultExceptionHandler.STORAGE_ITEM_NOT_FOUND);
    }

    return result;
  }
}

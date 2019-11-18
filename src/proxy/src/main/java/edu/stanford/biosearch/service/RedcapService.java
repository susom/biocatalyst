package edu.stanford.biosearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.config.DefaultExceptionHandler;
import edu.stanford.biosearch.data.redcap.RedcapRestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedcapService {
  @Autowired
  private HttpServletRequest request;

  @Autowired
  RedcapRestClient redcapRestClient;

  public List<Map<String, Object>> getRedcapProjects() throws IOException {
    Map<String, Object> apiResult = redcapRestClient.getRedcapProjects();

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) != 200) {
      throw new IOException("Redcap API has returned an invalid project list.");
    } else {
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> list = mapper.readValue(apiResult.get(RedcapRestClient.RESPONSE_BODY).toString(), new TypeReference<List<Map<String, Object>>>() {
      });

      for (Map<String, Object> userMap : list) {
        // projects is null if user has no projects/redcap access
        if (userMap.get("user").toString().equals(apiResult.get(RedcapRestClient.REQUEST_USER)) && userMap.get("projects") != null) {
          return (List<Map<String, Object>>) userMap.get("projects");
        }
      }
    }

    return new ArrayList<>(); // if user has no redcap projects

  }

  public Map<String, Object> getRedcapReports(String projectID) throws Exception {
    Map<String, Object> apiResult = redcapRestClient.getRedcapReports(projectID);

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) == 403) {
      throw new Exception(DefaultExceptionHandler.REDCAP_PROJECT_UNAUTHORIZED);
    }

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) == 404) {
      throw new Exception(DefaultExceptionHandler.REDCAP_PROJECT_NOT_FOUND);
    }

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) != 200) {
      throw new IOException("Redcap API has returned an invalid project list.");
    }

    ObjectMapper mapper = new ObjectMapper();
    // Converting Response body into a Map<String, Object> as opposed to a string
    apiResult.put(RedcapRestClient.RESPONSE_BODY, mapper.readValue(apiResult.get(RedcapRestClient.RESPONSE_BODY).toString(), new TypeReference<Map<String, Object>>() {
    }));
    return apiResult;
  }

  public Map<String, Object> getRedcapColumns(String projectID, String reportID) throws Exception {
    Map<String, Object> apiResult = redcapRestClient.getRedcapColumns(projectID, reportID);

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) == 404) {
      throw new Exception(DefaultExceptionHandler.REDCAP_PROJECT_NOT_FOUND);
    }

    if ((int) apiResult.get(RedcapRestClient.RESPONSE_CODE) != 200) {
      throw new IOException("Redcap API has returned invalid Data.");
    }

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> apiResponseBody = mapper.readValue(apiResult.get(RedcapRestClient.RESPONSE_BODY).toString(), new TypeReference<Map<String, Object>>() {
    });
    List<Map<String, Object>> columns = (List<Map<String, Object>>) apiResponseBody.get("columns");

    ArrayList<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();

    columns.forEach(column -> {
      Map<String, Object> temp = new HashMap<String, Object>();
      temp.put("field_name", column.get("field_name"));
      temp.put("field_label", column.get("field_label"));
      returnList.add(temp);
    });

    Map<String, Object> results = new HashMap<String, Object>();
    results.put(RedcapRestClient.RESPONSE_BODY, returnList);
    results.put(RedcapRestClient.RESPONSE_CODE, apiResult.get(RedcapRestClient.RESPONSE_CODE));

    return results;
  }
}

package edu.stanford.biosearch.model.stride.metadata;

import edu.stanford.biosearch.util.StringJsonToClass;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataResponse {
  @Autowired
  StringJsonToClass stringJsonToClass;

  private Map<String, List<Object>> response = new HashMap<String, List<Object>>();

  public MetadataResponse(JSONObject response) {
    this.initialize(response);
  }

  public MetadataResponse(String Json) {
    JSONObject json = new JSONObject(Json);
    this.initialize(json);
  }

  private void initialize(JSONObject response) {
    Iterator<String> keys = response.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      this.response.put(key, (LinkedList<Object>) response.get(key));
    }
  }

}

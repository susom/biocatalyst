package edu.stanford.biosearch.model.integrateddata.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class Wildcard implements Filter {

  @JsonProperty("wildcard")
  public Map<String, Object> filter = new HashMap<String, Object>();

  public Object getValue(String key) {
    return this.filter.get(key);
  }

  public void setValue(String key, Object value) {
    this.filter.put(key, ((String) value).toLowerCase().concat("*"));
  }

}

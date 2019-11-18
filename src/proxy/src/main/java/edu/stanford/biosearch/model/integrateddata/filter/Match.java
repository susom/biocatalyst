package edu.stanford.biosearch.model.integrateddata.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class Match implements Filter {

  @JsonProperty("match")
  public Map<String, Object> filter = new HashMap<String, Object>();

  public Object getValue(String Key) {
    return this.filter.get(Key);
  }

  public void setValue(String Key, Object Value) {
    this.filter.put(Key, Value);
  }

}

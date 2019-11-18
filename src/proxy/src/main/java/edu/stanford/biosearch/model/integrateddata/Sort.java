package edu.stanford.biosearch.model.integrateddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class Sort {
  @JsonProperty("sort")
  private Map<String, String> sort = new HashMap<String, String>();

  public String getValue(String Key) {
    return this.sort.get(Key);
  }

  public void setValue(String Key, String Value) {
    this.sort.put(Key, Value);
  }

  public Map<String, String> getSort(String Key, String Value) {
    return this.sort;
  }

  public void setSort(Map Sorts) {
    this.sort = Sorts;
  }

}

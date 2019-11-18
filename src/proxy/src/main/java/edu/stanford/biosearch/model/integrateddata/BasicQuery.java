package edu.stanford.biosearch.model.integrateddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BasicQuery {
  @JsonProperty("columns")
  private List<String> columns = new LinkedList<String>();

  @JsonProperty("from")
  private int from = 0;

  @JsonProperty("filters")
  private Map<String, Object> filters = new HashMap<String, Object>();

  @JsonProperty("size")
  private int size = 0;

  @JsonProperty("sort")
  private Map<String, String> sort = new HashMap<String, String>();

  @JsonProperty("indexes")
  private List<String> indexes = new LinkedList<String>();

  public List<String> getColumns() {
    return this.columns;
  }

  public int getFrom() {
    return this.from;
  }

  public Map<String, Object> getFilters() {
    return this.filters;
  }

  public List<String> getIndices() {
    return this.indexes;
  }

  public int getSize() {
    return this.size;
  }

  public Map<String, String> getSort() {
    return this.sort;
  }

}

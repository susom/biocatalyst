package edu.stanford.biosearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuggestionRequest {
  @JsonProperty("index")
  String index;

  @JsonProperty("field")
  String field;

  @JsonProperty("query")
  String query;

  public String getIndex() {
    return this.index;
  }

  public String getField() {
    return this.field;
  }

  public String getQuery() {
    return this.query;
  }
}

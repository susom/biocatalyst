package edu.stanford.biosearch.model.globalsearchrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GlobalSearchRequest {
  @JsonProperty("aggregations")
  boolean aggregations = false;

  @JsonProperty("highlights")
  boolean highlights = true;

  @JsonProperty("indexMappings")
  boolean indexMappings = false;

  @JsonProperty("indexNames")
  boolean indexNames = false;

  @JsonProperty("from")
  int from = 0;

  @JsonProperty("size")
  int size = 100;

  @JsonProperty("indices")
  List<String> indices;

  @JsonProperty("query")
  String query;

  @JsonProperty("filters")
  List<Object> filters;

  @JsonProperty("sources")
  List<String> sources;

  @JsonProperty("suggestionField")
  String suggestionField = "";

  public boolean getAggregations() {
    return this.aggregations;
  }

  public boolean getHighlights() {
    return this.highlights;
  }

  public List<String> getSources() {
    return this.sources;
  }

  public boolean getIndexMappings() {
    return this.indexMappings;
  }

  public boolean getIndexNames() {
    return this.indexNames;
  }

  public int getFrom() {
    return this.from;
  }

  public int getSize() {
    return this.size;
  }

  public List<String> getIndices() {
    return this.indices;
  }

  public String getQuery() {
    return this.query;
  }

  public List<Object> getFilters() {
    return this.filters;
  }

  public String getSuggestionfield() {
    return this.suggestionField;
  }

}

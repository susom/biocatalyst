package edu.stanford.biosearch.model.bookmark;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;

public class Bookmark {
  // Deprecated
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("Filters")
  private List<Header> filters = new LinkedList<Header>();

  // Deprecated
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("VisableColumns")
  private List<Header> visableColumns = new LinkedList<Header>();

  @JsonProperty("IntegrationID")
  private String integrationID = "";

  @JsonProperty("Name")
  private String name = "";

  @JsonProperty("State")
  private Object state = null;

  @JsonProperty("_ids")
  private String id;

  @JsonProperty("Query")
  private String query = "";

  public String getID() {
    return this.id;
  }
}

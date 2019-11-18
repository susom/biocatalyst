package edu.stanford.biosearch.model.bookmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Header {
  @JsonProperty("Filter")
  private Object filter = new Object();

  @JsonProperty("Hidden")
  private boolean hidden = false;

  @JsonProperty("Label")
  private String label = "";

  @JsonProperty("Order")
  private int order = -1;

  @JsonProperty("SortOrder")
  private int sortOrder = 0;

  @JsonProperty("Type")
  private int type = 0;

}

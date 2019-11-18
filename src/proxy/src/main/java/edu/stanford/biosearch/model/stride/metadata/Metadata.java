package edu.stanford.biosearch.model.stride.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Metadata {

  @JsonProperty("data_type")
  public String accessToken;

  @JsonProperty("description")
  public String description;

  @JsonProperty("variable_name")
  public String variable_name;

  @JsonProperty("relation")
  public String relation;

}

package edu.stanford.biosearch.model.integrateddata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {

  @JsonProperty("bool")
  private Bool boolProperty = new Bool();

  public Bool getBool() {
    return this.boolProperty;
  }

}

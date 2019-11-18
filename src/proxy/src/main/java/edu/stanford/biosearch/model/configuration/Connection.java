package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connection {
  @JsonProperty("sourceId")
  private String sourceId;

  @JsonProperty("subjectCodeType")
  private String subjectCodeType;

  @JsonProperty("visitId")
  ConnectionVisitId visitId;

  @JsonProperty("matching_strategy")
  String matching_strategy;

  public String getMatching_strategy() {
    return this.matching_strategy;
  }

  public String getSourceId() {
    return this.sourceId;
  }

  public String getSubjectCodeType() {
    return this.subjectCodeType;
  }

  public ConnectionVisitId getVisitId() {
    return this.visitId;
  }
}

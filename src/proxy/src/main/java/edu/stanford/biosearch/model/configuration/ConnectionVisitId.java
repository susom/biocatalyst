package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionVisitId {
  @JsonProperty("type")
  String type;

  @JsonProperty("eventNameMapping")
  EventNameMapping eventNameMapping;

  @JsonProperty("dateMatchingRangeInDays")
  int dateMatchingRangeInDays;

  public String getType() {
    return this.type;
  }

  public EventNameMapping getEventNameMapping() {
    return this.eventNameMapping;
  }

  public int getDateMatchingRangeInDays() {
    return this.dateMatchingRangeInDays;
  }

}

package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventNameMappingRow {

  @JsonProperty("Event Name")
  private String eventName;

  @JsonProperty("BB Event Name")
  private String bbEventName;

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getBbEventName() {
    return bbEventName;
  }

  public void setBbEventName(String bbEventName) {
    this.bbEventName = bbEventName;
  }

}

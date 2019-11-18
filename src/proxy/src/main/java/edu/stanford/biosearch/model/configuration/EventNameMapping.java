package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class EventNameMapping {

  @JsonProperty("source")
  private String source;

  @JsonProperty("destination")
  private String destination;

  @JsonProperty("mapping")
  private Map<String, String> mapping = new HashMap<String, String>();

  public String getSource() {
    return this.source;
  }

  public String getDestination() {
    return this.destination;
  }

  public Map<String, String> getMapping() {
    return mapping;
  }

}

package edu.stanford.biosearch.model.stride.metadata.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.biosearch.model.stride.metadata.Metadata;

public class Demographics extends Metadata {
  @JsonProperty("data_type")
  public String data_type;
}

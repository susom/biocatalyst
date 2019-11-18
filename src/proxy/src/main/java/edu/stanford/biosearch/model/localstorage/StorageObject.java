package edu.stanford.biosearch.model.localstorage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

@JsonDeserialize(using = StorageObjectDeserializer.class)
public class StorageObject {
  @JsonProperty("type")
  String type;

  @JsonProperty("data")
  Map<String, Object> data;

  public String getType() {
    return this.type;
  }

  public Map<String, Object> getData() {
    return this.data;
  }

  @Override
  public String toString() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }
}

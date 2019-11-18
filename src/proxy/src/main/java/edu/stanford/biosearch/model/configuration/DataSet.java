package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedList;
import java.util.List;
import org.springframework.lang.Nullable;

@JsonPropertyOrder( {"name", "_ids", "dataSources"})
public class DataSet {
  @JsonProperty
  private String name;

  @JsonProperty("_ids")
  @Nullable
  private String id;

  @JsonProperty
  private List<DataSource> dataSources = new LinkedList<>();

  private boolean valid;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<DataSource> getDataSources() {
    return dataSources;
  }

  public void addDataSource(DataSource dataSource) {
    this.dataSources.add(dataSource);
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }
}

package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {"source_id", "name", "type", "connection_details", "integration_details",
    "matching_strategy", "processed_id", "deletable", "valid", "primary", "matching_strategy", "processed_id"})
public class DataSource {

  @JsonProperty("source_id")
  private String source_id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("type")
  private SourceType type;

  @JsonProperty("connection_details")
  private ConnectionDetails connDetails;

  @JsonProperty("integration_details")
  private IntegrationDetails integrationDetails;

  @JsonProperty("matching_strategy")
  private String matching_strategy;

  @JsonProperty("processed_id")
  private String processed_id;

  @JsonProperty("deletable")
  private boolean deletable;

  @JsonProperty("valid")
  private boolean valid;

  @JsonProperty("primary")
  private boolean primary;

  public String getProcessed_id() {
    return processed_id;
  }

  public void setProcessed_id(String processed_id) {
    this.processed_id = processed_id;
  }

  public String getSource_id() {
    return source_id;
  }

  public void setSource_id(String sourceId) {
    this.source_id = sourceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SourceType getType() {
    return type;
  }

  public void setType(SourceType type) {
    this.type = type;
  }

  public ConnectionDetails getConnDetails() {
    return connDetails;
  }

  public void setConnDetails(ConnectionDetails connDetails) {
    this.connDetails = connDetails;
  }

  public IntegrationDetails getIntegrationDetails() {
    return integrationDetails;
  }

  public void setIntegrationDetails(IntegrationDetails integrationDetails) {
    this.integrationDetails = integrationDetails;
  }

  public boolean isDeletable() {
    return deletable;
  }

  public void setDeletable(boolean isDeletable) {
    this.deletable = isDeletable;
  }

  public boolean isValid() {
    return this.valid;
  }

  public void setValid(boolean isValid) {
    this.valid = isValid;
  }

  public boolean isPrimary() {
    return this.primary;
  }

  public void setPrimary(boolean isPrimary) {
    this.primary = isPrimary;
  }

  public String getMatching_strategy() {
    return this.matching_strategy;
  }

}

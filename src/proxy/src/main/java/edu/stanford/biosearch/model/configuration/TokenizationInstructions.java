package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TokenizationInstructions {

  @JsonProperty("type")
  private String type;

  @JsonProperty("columnLabel")
  private String columnLabel;

  @JsonProperty("columnName")
  private String columnName;

  @JsonProperty("delimiter_positions")
  private List<String> delimiter_positions;

  @JsonProperty("important_tokens")
  private List<String> important_tokens;

  @JsonProperty("important_tokens_order")
  private List<String> important_tokens_order;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("growth_tokens")
  private List<String> growth_tokens;

  @JsonProperty("processed_id")
  private String processed_id;

  @JsonProperty("configurableColumn")
  private boolean configurableColumn;

  @JsonProperty("configurableType")
  private boolean configurableType;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("removeable")
  private boolean removeable;

  // Optional. Use Case: Visit Date Format
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("format")
  private String format;

  // Optional. Use Case: Visit Date Format. For UI Purposes
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("format_moment")
  private String format_moment;

  // type
  public String getType() {
    return this.type;
  }

  public void setType(String Type) {
    this.type = Type;
  }

  // columnName
  public String getColumnLabel() {
    return this.columnLabel;
  }

  public void setColumnLabel(String Label) {
    this.columnLabel = Label;
  }

  // columnName
  public String getColumnName() {
    return this.columnName;
  }

  public void setColumnName(String Name) {
    this.columnName = Name;
  }

  // important_tokens
  public List<String> getImportant_tokens() {
    return this.important_tokens;
  }

  // important_tokens_order
  public List<String> getImportant_tokens_order() {
    return this.important_tokens_order;
  }

  public void setImportant_tokens_order(List<String> TokenOrder) {
    this.important_tokens_order = TokenOrder;
  }

  // growthTokens
  public List<String> getGrowth_tokens() {
    return this.growth_tokens;
  }

  public void setGrowth_tokens(List<String> GrowthTokens) {
    this.growth_tokens = GrowthTokens;
  }

  //delimiter_positions
  public List<String> getDelimiter_positions() {
    return this.delimiter_positions;
  }

  public void setDelimiter_positions(List<String> TokenOrder) {
    this.delimiter_positions = TokenOrder;
  }

  //delimiter_positions
  public String getProcessed_id() {
    return this.processed_id;
  }

  public void setProcessed_id(String Processed_id) {
    this.processed_id = Processed_id;
  }

  public boolean getConfigurableColumn() {
    return this.configurableColumn;
  }

  public boolean getConfigurableType() {
    return this.configurableType;
  }

  // removable
  public boolean getRemoveable() {
    return this.removeable;
  }

  // format
  public void setFormat(String value) {
    this.format = value;
  }

  public String getFormat() {
    return this.format;
  }

  // format_original
  public void setFormat_moment(String value) {
    this.format_moment = value;
  }

  public String getFormat_moment() {
    return this.format_moment;
  }

}

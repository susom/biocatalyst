package edu.stanford.integrator.importer.redcap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedcapApiColumn {
  private String form_name;
  private String field_name;
  private String field_order;
  private String field_label;
  private String field_type;
  private String field_options;
  private String field_validation;

  public RedcapApiColumn() {
  }

  // Getters & Setters
  public String getForm_name() {
    return this.form_name;
  }

  public void setForm_name(String value) {
    this.form_name = value;
  }

  public String getField_name() {
    return this.field_name;
  }

  public void setField_name(String value) {
    this.field_name = value;
  }

  public String getField_order() {
    return this.field_order;
  }

  public void setField_order(String value) {
    this.field_order = value;
  }

  public String getField_label() {
    return this.field_label;
  }

  public void setField_label(String value) {
    this.field_label = value;
  }

  public String getField_type() {
    return this.field_type;
  }

  public void setField_type(String value) {
    this.field_type = value;
  }

  public String getField_validation() {
    return this.field_validation;
  }

  public void setField_validation(String value) {
    this.field_validation = value;
  }

  public String getField_options() {
    return this.field_options;
  }

  public void setField_options(String value) {
    this.field_options = value;
  }

  public List<String> FieldOptions() {
    List<String> options = new ArrayList<String>();
    if (this.field_options != null) {
      String[] tokens = this.field_options.split("\\\\n");
      // Trim White Space
      for (int index = 0; index < tokens.length; index++) {
        tokens[index] = tokens[index].trim();
      }

      options.addAll(Arrays.asList(tokens));
    }
    return options;
  }

}

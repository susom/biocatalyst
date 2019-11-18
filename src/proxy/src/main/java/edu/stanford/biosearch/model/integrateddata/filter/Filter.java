package edu.stanford.biosearch.model.integrateddata.filter;

public interface Filter {
  Object getValue(String Key);

  void setValue(String Key, Object Value);
}

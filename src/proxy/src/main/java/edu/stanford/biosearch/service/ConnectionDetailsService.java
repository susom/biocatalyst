package edu.stanford.biosearch.service;

import edu.stanford.biosearch.model.configuration.DataSource;

public interface ConnectionDetailsService {
  void setConnectionDetails(DataSource dataSource, String message);
}

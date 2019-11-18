package edu.stanford.integrator.services;

import java.util.List;

public interface ValidateService {
  Boolean validate(String configId) throws Exception;

  List<String> getFailedSourceIds() throws Exception;

  void addFailedSourceIds(String value) throws Exception;
}

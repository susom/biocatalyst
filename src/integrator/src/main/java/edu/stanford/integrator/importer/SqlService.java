package edu.stanford.integrator.importer;

public interface SqlService {
  void createSqlField(String fieldName);

  void createSqlInsertStatement(String fieldName, String value);
}

package edu.stanford.integrator.repository;

public interface DatabaseService {
  int executeSqlQuery(String sql) throws Exception;

  int executeSqlDrop(String tableName);
}

package edu.stanford.integrator.repository;

import edu.stanford.integrator.services.IntegratorServiceImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@Repository
public class DatabaseServiceImpl implements DatabaseService {
  private static final Logger LOGGER = Logger.getLogger(IntegratorServiceImpl.class.getName());

  @PersistenceContext
  private EntityManager em;

  @Override
  public int executeSqlQuery(String sql) {
    return em.createNativeQuery(sql).executeUpdate();
  }

  @Override
  public int executeSqlDrop(String tableName) {
    String sqlDrop = "DROP TABLE  " + tableName;
    return em.createNativeQuery(sqlDrop).executeUpdate();
  }
}
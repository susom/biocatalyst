package edu.stanford.biosearch.service;

import edu.stanford.biosearch.model.configuration.ConnectionDetails;
import edu.stanford.biosearch.model.configuration.Credential;
import edu.stanford.biosearch.model.configuration.DataSource;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class CsvConnectionDetailsService implements ConnectionDetailsService {
  private static final Logger logger = Logger.getLogger(CsvConnectionDetailsService.class);

  @Override
  public void setConnectionDetails(DataSource dataSource, String message) {
    ConnectionDetails connDetails = dataSource.getConnDetails();
    Credential cred = connDetails.getCredential();

    if (cred != null) {
      logger.warn("CSV Credentials Not Accepted. Removing CSV Credentials");
      if (connDetails.getUrl() != null) {
        logger.warn("CSV URL Not Accepted. Removing CSV URL");
      }

      connDetails.setCredential(null);
      connDetails.setUrl(null);

    }
  }
}

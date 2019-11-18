package edu.stanford.biosearch.service;

import edu.stanford.biosearch.model.configuration.ConnectionDetails;
import edu.stanford.biosearch.model.configuration.Credential;
import edu.stanford.biosearch.model.configuration.DataSource;
import org.springframework.stereotype.Service;

@Service
public class EpicConnectionDetailsService implements ConnectionDetailsService {
  @Override
  public void setConnectionDetails(DataSource dataSource, String message) {
    ConnectionDetails connDetails = dataSource.getConnDetails();
    Credential cred = connDetails.getCredential();
    if (cred != null) {
      throw new IllegalArgumentException("Credentials are not accepted for source type: " + message);
    } else if (connDetails.getUrl() != null) {
      throw new IllegalArgumentException("URL is not accepted for source type: " + message);
    }
    cred = new Credential();
    connDetails.setCredential(cred);
  }
}

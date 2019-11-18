package edu.stanford.biosearch.service;

import edu.stanford.biosearch.model.configuration.ConnectionDetails;
import edu.stanford.biosearch.model.configuration.Credential;
import edu.stanford.biosearch.model.configuration.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenspecimenConnectionDetailsService implements ConnectionDetailsService {
  @Value("${openspecimen.rest.url}")
  private String openspecimenRestUrl;

  @Value("${openspecimen.query}")
  private String openspecimenQuery;

  @Value("${openspecimen.username}")
  private String openspecimenUsername;

  @Value("${openspecimen.password}")
  private String openspecimenPassword;

  @Override
  public void setConnectionDetails(DataSource dataSource, String message) {
    if (dataSource.isPrimary()) {
      ConnectionDetails connDetails = dataSource.getConnDetails();
      Credential cred = connDetails.getCredential();
      if (cred != null) {
        throw new IllegalArgumentException("Credentials are not accepted for source type: " + message);
      } else if (connDetails.getUrl() != null) {
        throw new IllegalArgumentException("URL is not accepted for source type: " + message);
      }
      connDetails.setUrl(openspecimenRestUrl + openspecimenQuery);
      cred = new Credential();
      cred.setUser(openspecimenUsername);
      cred.setPassword(openspecimenPassword);
      connDetails.setCredential(cred);
    } else {
      throw new IllegalArgumentException("OpenSpecimen may only be used as the primary (specimen) source.");
    }
  }
}

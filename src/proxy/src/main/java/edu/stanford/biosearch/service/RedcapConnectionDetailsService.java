package edu.stanford.biosearch.service;

import edu.stanford.biosearch.model.configuration.ConnectionDetails;
import edu.stanford.biosearch.model.configuration.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RedcapConnectionDetailsService implements ConnectionDetailsService {
  @Value("${redcap.api.url}")
  private String redcapApiUrl;

  @Value("${redcap.api.projects.url}")
  private String redcapProjectsUrl;

  @Override
  public void setConnectionDetails(DataSource dataSource, String message) {
    ConnectionDetails connDetails = dataSource.getConnDetails();
    if (connDetails.getUrl() != null) {
      throw new IllegalArgumentException("URL is not accepted for source type: " + message);
    }
    connDetails.setUrl(redcapApiUrl + redcapProjectsUrl);
  }
}

package edu.stanford.biosearch.service;

import edu.stanford.biosearch.data.openspecimen.OpenSpecimenRestClient;
import edu.stanford.biosearch.util.Base64Util;
import edu.stanford.biosearch.util.RemoteUser;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenSpecimenService {
  @Autowired
  Base64Util base64Util;

  @Autowired
  RemoteUser request;

  @Autowired
  OpenSpecimenRestClient openSpecimenRestClient;

  @Value("${openspecimen.collection.protocols}")
  private String collProtocolsUrl;

  @Value("${openspecimen.username}")
  private String username;

  @Value("${openspecimen.password}")
  private String password;

  public Map<String, Object> getCollectionProtocols() throws Exception {
    String credential = base64Util.encode(username, password);
    String remoteUser = request.getRemoteUser();
    Map<String, Object> result = openSpecimenRestClient.getCollectionProtocols(credential, collProtocolsUrl, remoteUser);

    return result;
  }
}

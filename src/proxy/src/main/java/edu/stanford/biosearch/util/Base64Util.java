package edu.stanford.biosearch.util;

import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class Base64Util {

  public String encode(String username, String password) {
    String credentialPlainText = username + ":" + password;
    String credentialEncoded = Base64.getEncoder().encodeToString(credentialPlainText.getBytes());
    String credential = "Basic " + credentialEncoded;
    return credential;
  }
}

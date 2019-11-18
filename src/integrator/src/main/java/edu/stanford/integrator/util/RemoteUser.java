package edu.stanford.integrator.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RemoteUser {
  @Autowired
  HttpServletRequest request;

  public String getRemoteUser() {
    return request.getRemoteUser();
  }

  public Object getDisplayName() {
    return this.request.getAttribute("displayName");
  }

  public Object getMail() {
    return request.getAttribute("mail");
  }
}

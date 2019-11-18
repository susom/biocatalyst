package edu.stanford.biosearch.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class RemoteUser {

  @Autowired
  HttpServletRequest request;

  @Autowired
  Build build;

  public String getRemoteUser() {
    return (build.StubbedResponses()) ? "test_user" : request.getRemoteUser();
  }

  public Object getDisplayName() {
    return (build.StubbedResponses()) ? "John Smith" : this.request.getAttribute("displayName");
  }

  public Object getMail() {
    return (build.StubbedResponses()) ? "exampleuser@stanford.edu" : request.getAttribute("mail");
  }
}

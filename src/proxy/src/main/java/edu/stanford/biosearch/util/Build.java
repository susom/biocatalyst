package edu.stanford.biosearch.util;

import org.springframework.stereotype.Service;

@Service
public class Build {

  // These will need to be read from a file elsewhere.
  private boolean stubbedResponses = false;

  // For supplementing fake responses. Internet-less testing
  public boolean StubbedResponses() {
    return this.stubbedResponses;
  }
}

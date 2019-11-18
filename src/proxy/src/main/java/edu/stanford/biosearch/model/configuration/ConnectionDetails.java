package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedList;
import java.util.List;

@JsonPropertyOrder( {"URL", "filename", "headers", "data", "credentials"})
public class ConnectionDetails {

  @JsonProperty("URL")
  private String url;
  private List<String> headers = new LinkedList<String>();
  private Object data;

  @JsonProperty("credentials")
  private Credential credential;

  private String filename;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public void addHeader(String header) {
    this.headers.add(header);
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public Credential getCredential() {
    return credential;
  }

  public void setCredential(Credential credential) {
    this.credential = credential;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

}

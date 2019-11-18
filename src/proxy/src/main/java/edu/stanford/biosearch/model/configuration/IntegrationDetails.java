package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class IntegrationDetails {

  @JsonProperty("subjectCode")
  private List<TokenizationInstructions> subjectCode;

  @JsonProperty("visitId")
  private List<TokenizationInstructions> visitId;

  @JsonProperty("connections")
  private List<Connection> connections;

  public List<Connection> getConnections() {
    return this.connections;
  }

  public void setConnections(List<Connection> SubjectCodes) {
    this.connections = SubjectCodes;
  }

  public List<TokenizationInstructions> getSubjectCode() {
    return this.subjectCode;
  }

  public void setSubjectCode(List<TokenizationInstructions> SubjectCodes) {
    this.subjectCode = SubjectCodes;
  }

  public List<TokenizationInstructions> getVisitId() {
    return this.visitId;
  }

  public void setVisitId(List<TokenizationInstructions> VisitIDs) {
    this.visitId = VisitIDs;
  }
}

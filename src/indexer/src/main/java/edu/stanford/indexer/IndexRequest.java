package edu.stanford.indexer;

public class IndexRequest {
  private String sql;
  private String pipelineId;
  private String indexName;
  private Status status = Status.WAITING;
  private String result;
  private boolean cleanIndex;

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getPipelineId() {
    return pipelineId;
  }

  public void setPipelineId(String pipelineId) {
    this.pipelineId = pipelineId;
  }

  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public Status getStatus() {
    return status;
  }

  public String getResult() {
    return result;
  }

  public void setStatusAndResult(Status status, String result) {
    this.result = result;
    this.status = status;
  }

  public boolean isCleanIndex() {
    return cleanIndex;
  }

  public void setCleanIndex(boolean cleanIndex) {
    this.cleanIndex = cleanIndex;
  }
}

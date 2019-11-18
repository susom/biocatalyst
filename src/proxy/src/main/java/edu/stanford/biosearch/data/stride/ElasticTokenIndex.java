package edu.stanford.biosearch.data.stride;

public enum ElasticTokenIndex {
  Stride("stridetokens"),
  Validator("validatortokens");

  private final String index;

  ElasticTokenIndex(String Index) {
    this.index = Index;
  }

  public String Value() {
    return this.index;
  }

}

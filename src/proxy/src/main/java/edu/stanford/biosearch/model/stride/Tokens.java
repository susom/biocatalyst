package edu.stanford.biosearch.model.stride;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tokens {

  @JsonProperty("accessToken")
  String accessToken;

  @JsonProperty("refreshToken")
  String refreshToken;

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getRefreshToken() {
    return this.refreshToken;
  }

}
package edu.stanford.integrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class ElasticConfig {
  @Value("${elastic.protocol}")
  public String protocol;
  @Value("${elastic.host}")
  public String host;
  @Value("${elastic.port}")
  public String port;
  @Value("${elastic.user}")
  public String user;
  @Value("${elastic.pass}")
  public String pass;
  @Value("${elastic.ssl}")
  public Boolean ssl;
  @Value("${elastic.sslCert}")
  public String sslCert;
}


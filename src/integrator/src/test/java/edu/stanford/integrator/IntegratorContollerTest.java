package edu.stanford.integrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegratorContollerTest {
  @LocalServerPort
  String port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private IntegratorController integratorController;
}

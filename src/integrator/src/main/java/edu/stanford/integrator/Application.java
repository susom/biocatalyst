package edu.stanford.integrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@CacheEvict
@PropertySources( {
    @PropertySource("classpath:application.properties"),
    @PropertySource("classpath:${environment}.properties"),
})
public class Application {
  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  public static void main(String[] args) {
    LOGGER.log(Level.INFO, "Running the service");
    SpringApplication.run(Application.class, args);
  }
}

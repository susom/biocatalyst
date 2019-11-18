package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.Integrator;
import edu.stanford.integrator.ServiceStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.stanford.integrator.ServiceStatus.State.INACTIVE;

@Service
public class IntegrateTask {

  private static final Logger LOGGER = Logger.getLogger(IntegrateTask.class.getName());

  @Autowired
  IntegratorService integratorService;

  @Value("${biosearch-proxy.url}")
  String biosearchProxyUrl;

  @Value("${integrator.task.timeout.minutes}")
  private int timeout;

  private static final String ELASTIC_ROLE_URL = "/elastic/role";

  public void integrate(Integrator integrator, String universityId) {
    Callable<ServiceStatus> task = () -> {
      ServiceStatus status = null;
      try {
        status = integratorService.integrateTask(integrator, universityId);
        if (status != null) {
          if (status.getState().equals(ServiceStatus.State.INACTIVE) && status.getInfo().contains(IntegratorService.INTEGRATION_SUCCESS)) {
            try {
              addIndexToRole(integrator.getIntegrationID(), universityId);
            } catch (Exception e) {
              integrator.setStatus(new ServiceStatus(INACTIVE, "Failed to grant the user access to this index. " + e.getLocalizedMessage()));
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        LOGGER.log(Level.INFO, e.getMessage());
        return null;
        //TODO: fix "Transaction marked as rollbackOnly" bug
        //throw new IllegalStateException("task interrupted " + e.getMessage(), e);
      }

      return status;

    };

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future future = executor.submit(task);

    try {
      future.get(timeout, TimeUnit.MINUTES);
    } catch (TimeoutException e) {
      future.cancel(true);
      integrator.setStatus(new ServiceStatus(INACTIVE, "Integration timed out. " + e.getLocalizedMessage()));
    } catch (InterruptedException e) {
      future.cancel(true);
      integrator.setStatus(new ServiceStatus(INACTIVE, "Integration was interrupted. " + e.getLocalizedMessage()));
    } catch (ExecutionException e) {
      future.cancel(true);
      integrator.setStatus(new ServiceStatus(INACTIVE, "Integration failed. " + e.getLocalizedMessage()));
    }

    try {
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      System.err.println("tasks interrupted");
    } finally {
      if (!executor.isTerminated()) {
        System.err.println("cancel non-finished tasks");
      }
      executor.shutdownNow();
    }
  }

  private HttpResponse addIndexToRole(String index, String universityId) throws IOException {
    Map<String, String> body = new HashMap<>();
    body.put("index", index);
    body.put("role", universityId + "_role");
    HttpPut put = new HttpPut(biosearchProxyUrl + ELASTIC_ROLE_URL);
    String json = new ObjectMapper().writeValueAsString(body);
    put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    HttpClient httpClient = HttpClientBuilder.create().build();
    LOGGER.log(Level.INFO, "Adding index to role");
    return httpClient.execute(put);
  }
}

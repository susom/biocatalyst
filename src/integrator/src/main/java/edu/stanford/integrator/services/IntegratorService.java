package edu.stanford.integrator.services;

import edu.stanford.integrator.Integrator;
import edu.stanford.integrator.ServiceStatus;

public interface IntegratorService {

  public static final String INTEGRATION_SUCCESS = "Integration completed";

  ServiceStatus integrateTask(Integrator integrator, String remoteUser);

}

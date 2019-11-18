package edu.stanford.integrator;

import static edu.stanford.integrator.ServiceStatus.State.UNKNOWN;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class IntegrationsMonitor {

  private final static Logger LOGGER = Logger.getLogger(IntegrationsMonitor.class.getName());
  private final Map<String, Integrator> integrations = new HashMap<>();

  public Integrator getIntegrator(String integratorID) {
    return integrations.get(integratorID);
  }

  public void setIntegrator(String integratorID, Integrator integator) {
    integrations.put(integratorID, integator);
  }

  public void removeIntegrator(String integratorID) {
    integrations.remove(integratorID);
  }

  public Integrator createIntegrator(String configID) throws Exception {
    Integrator integrator = null;
    synchronized (integrations) {
      integrator = getIntegrator(configID);
      if (integrator == null) {

        try {
          integrator = new Integrator(configID);
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, "Error creating integrator for integration " + configID + "\n", ex);
          throw new Exception("Unable to create a new Integrator object for " + configID + "\n" + ex.getLocalizedMessage());
        }

        // register the integrator
        try {
          setIntegrator(configID, integrator);
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, "Unable to register a new Integrator with ID: " + configID + "\n", ex);
          throw new Exception("Unable to register a new Integrator  \n" + ex.getLocalizedMessage());
        }
      }
    }

    return integrator;
  }

  public ServiceStatus getIntegratorStatus(String integratorID) {
    Integrator integrator = integrations.get(integratorID);
    if (integrator == null) {
      return new ServiceStatus(UNKNOWN);
    }

    return integrator.getStatus();
  }
}

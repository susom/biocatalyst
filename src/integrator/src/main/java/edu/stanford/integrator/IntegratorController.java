
package edu.stanford.integrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.integrator.config.DataSourceConfig;
import edu.stanford.integrator.repository.DatabaseService;
import edu.stanford.integrator.services.IntegrateTask;
import edu.stanford.integrator.services.PreprocessorService;
import edu.stanford.integrator.services.SampleFieldService;
import edu.stanford.integrator.services.ValidateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.stanford.integrator.ServiceStatus.State.ACTIVE;


@RestController
public class IntegratorController {
	@Autowired
	IntegrationsMonitor integrationsMonitor;

	@Autowired
  IntegrateTask integrateTask;

	@Autowired
  PreprocessorService preprocessorServiceImpl;

	@Autowired
  SampleFieldService sampleFieldServiceImpl;

	private static final Logger LOGGER = Logger.getLogger(IntegratorController.class.getName());

	// When a new source is added - grab one example from the ID field and return a tokenized version
	@RequestMapping(value = "/preprocessor", method = RequestMethod.GET)
	public Map<String,Object> preprocessor(@RequestParam(value = "config", required = true) String configID,
										   @RequestParam(value = "source_id", required = true) String sourceID,
										   @RequestParam(value = "field", required = true) String field) throws Exception {

		if (configID.length() == 0) {
			throw new IllegalArgumentException("Please specify the integration configuration id in the request.");
		}
		String remoteUser = "TODO";  // TODO
		LOGGER.log(Level.INFO, "Beginning pre-processing of configuration: " + configID + ", using source_id: " +
				sourceID + "...");

		// setters for request params
		preprocessorServiceImpl.setConfigId(configID);

		try {
			DataSourceConfig sourceConfig = preprocessorServiceImpl.getDataSourceConfig(remoteUser, sourceID);
			String json = preprocessorServiceImpl.getFullDataAsJsonString(remoteUser, sourceConfig);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(json);
			String sourceType = sourceConfig.getSourceType().toLowerCase();
			String sampleString = "";
			sampleFieldServiceImpl.setDataSourceConfig(sourceConfig);
			sampleFieldServiceImpl.setJson(jsonNode);
			switch (sourceType) {
				case "openspecimen":
					sampleString = sampleFieldServiceImpl.sampleOpenspecimen(field, 0);
					break;
				case "redcap":
					sampleString = sampleFieldServiceImpl.sampleRedcap(field, 0);
					break;
				case "csv":
					sampleString = sampleFieldServiceImpl.sampleCsv(field, 0);
					break;
			}
			return preprocessorServiceImpl.createTokenizationAndRawStringObject(sampleString);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Error preprocessing input: ", ex);
			throw new Exception("Error preprocessing input. " + ex.getLocalizedMessage());
		}
	}

	// Evaluate the relationship between a given source_id and the primary source_id (source_id=0)
	@RequestMapping(value = "/evaluation", method = RequestMethod.GET)
	public Map<String,Object> evaluation(@RequestParam(value = "config", required = true) String configID,
										 @RequestParam(value = "source_id", required = true) String sourceID) throws Exception {

		String remoteUser = "TODO"; // TODO
		if (configID.length() == 0) {
			throw new IllegalArgumentException("Please specify the integration configuration id in the request.");
		}

		LOGGER.log(Level.INFO, "Evaluating strategy for config ID: " + configID + ", source ID:" + sourceID + "...");

		// setters for request params
		preprocessorServiceImpl.setConfigId(configID);
		try {
			return preprocessorServiceImpl.determineImportantTokensOrder(remoteUser, sourceID);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Error evaluating important token order: ", ex);
			throw new Exception("Error evaluating important token order.");
		}
	}

	@RequestMapping(value = "/integrate", method = RequestMethod.POST)
	public ServiceStatus integrate(@RequestParam(value = "config", required = true) String configID, HttpServletRequest request) throws Exception {
		ValidateServiceImpl validateServiceImpl = new ValidateServiceImpl();
		String remoteUser = "TODO"; // TODO

		if (configID.length() == 0) {
			throw new IllegalArgumentException("Please specify the integration configuration id in the request.");
		}

		if (validateServiceImpl.validate(configID) == false) {
			// TODO: this always returns true for now for local development - but shouldn't
			List<String> failedSrcs = validateServiceImpl.getFailedSourceIds();
			throw new Exception("{\"error\": \"Invalid configuration. Configuration contains sources without valid matching_strategies. Integration will fail.\", \"failed_sources\":" + failedSrcs + "}");
		}

		LOGGER.log(Level.INFO, "Integrating configuration " + configID + " ...");

		Integrator integrator = integrationsMonitor.createIntegrator(configID);

		synchronized(integrator){

			ServiceStatus integratorStatus = integrator.getStatus();
			if (integratorStatus.getState().equals(ACTIVE)){
				return integratorStatus;
			} else {
				ServiceStatus status = new ServiceStatus(ACTIVE, "Integrating data sources... (configuration id: " + configID + ")");
				integrator.setStatus(status);
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					LOGGER.log(Level.INFO, "Runnable: Running");
					integrateTask.integrate(integrator, remoteUser);
					LOGGER.log(Level.INFO, "Runnable: Closed");
				}
			}).start();

			return integrator.getStatus();
		}
	}

	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public ServiceStatus status(@RequestParam(value = "config", required = true) String configID) {
		if (configID.length() == 0) {
			throw new IllegalArgumentException("Please specify the integration configuration id in the request.");
		}

		ServiceStatus status = integrationsMonitor.getIntegratorStatus(configID);
		return status;

	}

	@Autowired
	DatabaseService databaseService;
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	@Transactional
	public int test(@RequestBody String sql) throws Exception {
		return databaseService.executeSqlQuery(sql);
	}
}

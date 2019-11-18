package edu.stanford.integrator.services;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.config.IntegrationConfig;
import edu.stanford.integrator.config.IntegrationConfigDBProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ValidateServiceImpl implements ValidateService {

    private static final Logger LOGGER = Logger.getLogger(ValidateService.class.getName());

    @Autowired
    IntegrationConfigDBProxy integrationConfigDBProxy;

    @Value("${biosearch-proxy.url}")
    private String biosearchProxyUrl;

    private String configId;
    private List<String> failedSourceIds;

    public ValidateServiceImpl() {
        this.failedSourceIds = new ArrayList<>();
    }

    public List<String> getFailedSourceIds() {
        return this.failedSourceIds;
    }

    public void addFailedSourceIds(String value) {
        this.failedSourceIds.add(value);
    }

    private IntegrationConfig readConfiguration(String configId) throws Exception {
        LOGGER.log(Level.SEVERE, "about to readConfiguration.  " + configId);

        final JsonNode configJson = integrationConfigDBProxy.getIntegrationConfig(configId);
        LOGGER.log(Level.SEVERE, "done readConfiguration.");

        if (configJson == null) {
            LOGGER.log(Level.SEVERE, "The configuration for integration " + configId + " is missing.");
            throw new Exception("The configuration for integration " + configId + " is missing.");
        }
        return new IntegrationConfig(configJson);
    }

    public Boolean validate(String configId) throws Exception {
        return true;  // TODO
/*        try {

            final IntegrationConfig integrationConfig = readConfiguration(configId);
            LOGGER.log(Level.SEVERE, "got integrationconfig" );

            List<String> sourceIds = integrationConfig.getSourceIDs();
            LOGGER.log(Level.SEVERE, "src ids" + sourceIds.toString() );
            sourceIds.remove("0"); // remove the primary source, as it's matching_strategy is always null because we know how it relates to itself

            for (String id : sourceIds) {
                LOGGER.log(Level.SEVERE, "String id: " + id);

                DataSourceConfig sourceConfig = integrationConfig.getSourceConfig(id);
                if (sourceConfig.getMatchingStrategy().equals("") || sourceConfig.getMatchingStrategy().equals("unknown")) {
                    this.addFailedSourceIds(id);
                }
            }

            return this.getFailedSourceIds().isEmpty();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception: " + ex);
            return false;
        }*/
    }
}
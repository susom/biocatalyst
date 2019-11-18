package edu.stanford.integrator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LogstashJobQueueInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    LogstashJobQueueImpl logstashJobQueueImpl;

    private static final Logger LOGGER = Logger.getLogger(LogstashJobQueueInitializer.class.getName());

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.log(Level.INFO, "Initializing object pool for Logstash");
        try {
            logstashJobQueueImpl.setupPool(3); //TODO make this a property with a configurable value
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



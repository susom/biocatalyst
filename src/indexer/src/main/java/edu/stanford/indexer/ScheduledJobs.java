package edu.stanford.indexer;

import java.util.NoSuchElementException;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobs {
  Logger logger = Logger.getLogger(ScheduledJobs.class.getName());

  @Autowired
  Indexer indexer;

  @Scheduled(fixedDelay = 500)
  public void indexJobsInQueue() {
    logger.info("Processing Jobs In Queue");
    try {
      IndexRequest indexRequest = indexer.indexRequestQueue.removeFirst();
      indexer.executeIndex(indexRequest);
    } catch (NoSuchElementException e) {
      logger.warning(e.getMessage());
    }
  }
}

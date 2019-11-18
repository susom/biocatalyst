package edu.stanford.biosearch.data.elasticsearch;

import edu.stanford.biosearch.model.localstorage.StorageObject;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageClient {
  private static final Logger logger = Logger.getLogger(StorageClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  public Map<String, Object> Create(StorageObject obj) throws IOException {
    logger.info("Creating New Storage Object");
    return this.Update(obj, "");

  }

  public Map<String, Object> Update(StorageObject obj, String document) throws IOException {
    logger.info("Updating Storage Object" + document);
    String url = this.elasticNetwork.elasticsearchUrl
        .concat(this.elasticNetwork.storageIndex)
        .concat(this.elasticNetwork.storageType)
        .concat("/").concat(document);

    return this.elasticNetwork.post(url, obj.toString());
  }

  public Map<String, Object> Get(String Item_ID) throws IOException {
    logger.info("Getting Storage Object" + Item_ID);
    String url = this.elasticNetwork.elasticsearchUrl
        .concat(this.elasticNetwork.storageIndex)
        .concat(this.elasticNetwork.storageType)
        .concat("/").concat(Item_ID)
        .concat("/").concat("_source");

    return this.elasticNetwork.get(url);
  }

  public Map<String, Object> Delete(String Item_ID) throws IOException {
    logger.info("Deleting Storage Object" + Item_ID);
    String url = this.elasticNetwork.elasticsearchUrl
        .concat(this.elasticNetwork.storageIndex)
        .concat(this.elasticNetwork.storageType)
        .concat("/").concat(Item_ID);

    return this.elasticNetwork.delete(url);
  }

}

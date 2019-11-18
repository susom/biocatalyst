package edu.stanford.biosearch.service;

import edu.stanford.biosearch.data.stride.StrideRestClient;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StrideService {
  @Autowired
  StrideRestClient strideRestClient;

  public Map<String, Object> getIRBs() throws Exception {
    return this.strideRestClient.getIRBs();
  }

  public Map<String, Object> getMetadata(int IRB) throws Exception {
    Map<String, Object> result = this.strideRestClient.getMetaData(IRB);
    System.out.println("getMetadata:" + result);
    if ((int) result.get("responseCode") == 400) {
      throw new Exception("Bad Request: 400");
    }

    if ((int) result.get("responseCode") != 200) {
      throw new Exception("Bad Request");
    }

    return result;
  }
}
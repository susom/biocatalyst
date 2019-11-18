package edu.stanford.biosearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticsService {
  public HashMap getDiagnostics() throws Exception {
    JSONObject result = new JSONObject();
    result.put("status", new JSONObject()
        .put("Biosearch-Proxy", "Running")
        .put("Elastic Search", "Status not yet supported")
        .put("Open-Specimen", "Status not yet supported")
        .put("Redcap", "Status not yet supported")
    );
    return new ObjectMapper().readValue(result.toString(), HashMap.class);
  }
}

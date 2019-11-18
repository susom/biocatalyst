package edu.stanford.integrator.transform.visitid;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.integrator.transform.TransformWithUserCsvService;

public interface VisitIdTransformService extends TransformWithUserCsvService {
  String getVisitIdSource();

  String getVisitIdDestination();

  JsonNode getVisitIdConnectionsMapping();
}

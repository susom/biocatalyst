package edu.stanford.biosearch.controller;

import edu.stanford.biosearch.service.OpenSpecimenService;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class OpenSpecimenController {
  private static final Logger logger = Logger.getLogger(OpenSpecimenController.class);

  @Autowired
  OpenSpecimenService openSpecimenService;

  @RequestMapping(method = RequestMethod.GET, value = "/collection-protocols")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getPatients() throws Exception {
    Map<String, Object> result = openSpecimenService.getCollectionProtocols();
    return result;
  }
}

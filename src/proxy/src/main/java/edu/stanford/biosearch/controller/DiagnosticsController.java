package edu.stanford.biosearch.controller;

import edu.stanford.biosearch.service.DiagnosticsService;
import edu.stanford.biosearch.util.StringJsonToClass;
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
public class DiagnosticsController {
  private static final Logger logger = Logger.getLogger(DiagnosticsController.class);

  @Autowired
  DiagnosticsService diagnosticsService;

  @Autowired
  StringJsonToClass stringJsonToClass;

  @RequestMapping(method = RequestMethod.GET, value = "/diagnostics")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getDiagnostics() throws Exception {
    return diagnosticsService.getDiagnostics();
  }
}

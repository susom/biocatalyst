package edu.stanford.biosearch.controller;

import edu.stanford.biosearch.service.StrideService;
import edu.stanford.biosearch.service.TokenService;
import edu.stanford.biosearch.util.StringJsonToClass;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class StrideController {
  private static final Logger logger = Logger.getLogger(StrideController.class);

  @Autowired
  StrideService strideService;

  @Autowired
  TokenService tokenService;

  @Autowired
  StringJsonToClass stringJsonToClass;

  @RequestMapping(method = RequestMethod.GET, value = "/stride/irbs")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> irbs() throws Exception {
    return strideService.getIRBs();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/stride/metadata/{irb}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> metadata(@PathVariable("irb") int irb) throws Exception {
    return strideService.getMetadata(irb);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/stride/getStrideTokens")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getStrideTokens() throws Exception {
    return this.tokenService.getStrideTokens();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/stride/getValidatorTokens")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getValidatorTokens() throws Exception {
    return this.tokenService.getValidatorTokens();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/stride/refreshStrideTokens")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> refreshStrideTokens() throws Exception {
    return this.tokenService.refreshStrideTokens();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/stride/refreshValidatorTokens")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> refreshValidatorTokens() throws Exception {
    return this.tokenService.refreshValidatorTokens();
  }
}

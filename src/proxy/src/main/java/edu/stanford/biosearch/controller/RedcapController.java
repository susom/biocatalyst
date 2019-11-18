package edu.stanford.biosearch.controller;

import edu.stanford.biosearch.service.RedcapService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class RedcapController {
  @Autowired
  RedcapService redcapService;

  /**
   * Gets all the redcap projects the user has access to
   *
   * @return
   */
  @RequestMapping(value = "/redcap/projects", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<Map<String, Object>> getRedcapProjects() throws IOException {
    return redcapService.getRedcapProjects();
  }

  /**
   * Gets all the redcap reports the user has access to
   *
   * @return
   */
  @RequestMapping(value = "/redcap/reports/{projectID}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getRedcapReports(@PathVariable("projectID") String projectID) throws Exception {
    return redcapService.getRedcapReports(projectID);
  }

  /**
   * Gets all the redcap reports the user has access to
   *
   * @return
   */
  @RequestMapping(value = "/redcap/columns/{projectID}/{report_id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getRedcapColumns(
      @PathVariable("projectID") String projectID,
      @PathVariable("report_id") String report_id
  ) throws Exception {

    if (projectID == null) {
      throw new Exception("Project ID Required");
    }

    if (report_id == null) {
      throw new Exception("Report ID Required");
    }

    return redcapService.getRedcapColumns(projectID, report_id);
  }
}

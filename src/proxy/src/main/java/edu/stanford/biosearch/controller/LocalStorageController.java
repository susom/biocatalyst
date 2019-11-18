package edu.stanford.biosearch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.model.localstorage.StorageObject;
import edu.stanford.biosearch.service.LocalStorageService;
import edu.stanford.biosearch.util.StringJsonToClass;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/localstorage")
public class LocalStorageController {
  private static final Logger logger = Logger.getLogger(LocalStorageController.class);

  @Autowired
  StringJsonToClass stringJsonToClass;

  @Autowired
  LocalStorageService storage;

  @RequestMapping(method = RequestMethod.PUT, value = "/create")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> CreateStorage(@RequestBody String requestBody) throws Exception {
    StorageObject obj = this.ConvertBodyToStorageObject(requestBody);
    return storage.Create(obj);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> GetStorage(@PathVariable("id") String id) throws Exception {
    return storage.Get(id);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/preview/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> PreviewStorage(@PathVariable("id") String id) throws Exception {
    return storage.Preview(id);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/update/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> UpdateStorage(@PathVariable("id") String id, @RequestBody String requestBody)
      throws Exception {
    StorageObject obj = this.ConvertBodyToStorageObject(requestBody);
    return storage.Update(obj, id);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/delete/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> DeleteStorage(@PathVariable("id") String id) throws Exception {
    return storage.Delete(id);
  }

  private StorageObject ConvertBodyToStorageObject(String Body) throws IOException {
    String decodedBody = new String(Body.getBytes(StandardCharsets.ISO_8859_1));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(decodedBody);
    return mapper.treeToValue(jsonNode, StorageObject.class);
  }
}

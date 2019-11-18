package edu.stanford.indexer;

import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexerController {
  private Logger logger = Logger.getLogger(IndexerController.class.getName());

  @Autowired
  Indexer indexer;

  @RequestMapping(value = "/index", method = RequestMethod.POST)
  public String addIndex(@RequestParam(value = "clean", defaultValue = "false") Boolean cleanIndex,
                         @RequestBody(required = true) IndexRequest indexRequest) {

    if (indexRequest.getIndexName() == null || indexRequest.getIndexName().isEmpty()) {
      throw new IllegalArgumentException("Missing index name");
    }
    if (indexRequest.getSql() == null || indexRequest.getSql().isEmpty()) {
      throw new IllegalArgumentException("Missing sql");
    }

    if (indexRequest.getPipelineId() == null || indexRequest.getPipelineId().isEmpty()) {
      throw new IllegalArgumentException("Missing pipelineId");
    }

    if (!indexRequest.getIndexName().equals(indexRequest.getIndexName().toLowerCase())) {
      throw new IllegalArgumentException("Index name must be all lower-case.");
    }
    indexRequest.setCleanIndex(cleanIndex);

    return indexer.addIndexRequest(indexRequest);
  }

  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public IndexRequest getIndex(@RequestParam(required = true) String id) {
    return indexer.getIndexRequest(id);
  }

  @RequestMapping(value = "/index", method = RequestMethod.DELETE)
  public IndexRequest removeIndex(@RequestParam(required = true) String id) {
    return indexer.removeIndexRequest(id);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public @ResponseBody
  String handleException(Exception e) {
    return e.getMessage();
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  String handleException(MissingServletRequestParameterException e) {
    return e.getMessage();
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  String handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return "Http Message Not Readable";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  String handleIllegalArgumentException(IllegalArgumentException e) {
    return e.getMessage();
  }
}

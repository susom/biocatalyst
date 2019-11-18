package edu.stanford.biosearch.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {
  public static final String INVALID_BODY = "Invalid Body";
  public static final String STORAGE_ITEM_NOT_FOUND = "Item Not Found";
  public static final String INVALID_SOURCE_TYPE = "Invalid source type";
  public static final String CONFIGURATION_NOT_FOUND = "Configuration not found";
  public static final String REDCAP_PROJECT_NOT_FOUND = "Project not found";
  public static final String REDCAP_PROJECT_UNAUTHORIZED = "Unable to view this content";
  public static final String BOOKMARK_NOT_FOUND = "Bookmark not found";
  public static final String INTEGRATION_NOT_FOUND = "Integration not found";
  public static final String INDEX_NOT_FOUND = "Index not found";
  public static final String MAPPING_NOT_FOUND = "Mapping not found";
  public static final String CREDENTIALS_REQUIRED = "Credentials required";

  @ExceptionHandler( {Exception.class})
  public ResponseEntity<Object> handleDefaultException(Exception ex, WebRequest request) {
    if (ex.getMessage()
        .contains("Cannot deserialize value of type `edu.stanford.biosearch.model.configuration.SourceType`")) {
      return new ResponseEntity<Object>(INVALID_SOURCE_TYPE, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    if (ex.getMessage().equals(CONFIGURATION_NOT_FOUND)) {
      return new ResponseEntity<Object>(CONFIGURATION_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    if (ex.getMessage().equals(STORAGE_ITEM_NOT_FOUND)) {
      return new ResponseEntity<Object>(STORAGE_ITEM_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    if (ex.getMessage().equals(INTEGRATION_NOT_FOUND)) {
      return new ResponseEntity<Object>(INTEGRATION_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }
    if (ex.getMessage().equals(INDEX_NOT_FOUND)) {
      return new ResponseEntity<Object>(INDEX_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }
    if (ex.getMessage().equals(MAPPING_NOT_FOUND)) {
      return new ResponseEntity<Object>(MAPPING_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }
    if (ex.getMessage().equals(BOOKMARK_NOT_FOUND)) {
      return new ResponseEntity<Object>(BOOKMARK_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }
    if (ex.getMessage().equals(REDCAP_PROJECT_UNAUTHORIZED)) {
      return new ResponseEntity<Object>(REDCAP_PROJECT_UNAUTHORIZED, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }
    if (ex.getMessage().equals(REDCAP_PROJECT_NOT_FOUND)) {
      return new ResponseEntity<Object>(REDCAP_PROJECT_NOT_FOUND, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    if (ex.getMessage().equals(INVALID_BODY)) {
      return new ResponseEntity<Object>(INVALID_BODY, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<Object>(ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler( {IllegalArgumentException.class})
  public ResponseEntity<Object> handleIllegalArgumentException(Exception ex, WebRequest request) {
    return new ResponseEntity<Object>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }
}

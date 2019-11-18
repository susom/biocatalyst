package edu.stanford.biosearch.aspect;

import edu.stanford.biosearch.service.TokenService;
import edu.stanford.biosearch.util.RemoteUser;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StrideAspect {

  private static final Logger logger = Logger.getLogger(StrideAspect.class);

  @Autowired
  RemoteUser remoteUser;

  @Autowired
  TokenService tokenService;

  /**
   * Executes an HTTP request with the user's credentials
   *
   * @param pjp
   * @return
   * @throws Throwable
   */
  @Around("execution(*  edu.stanford.biosearch.data.stride.StrideExecutor.authorizedRequest(..))")
  public Object authenticatedHttpRequest(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Stride HTTP Execution");
    System.out.println("Stride HTTP Execution");
    HttpRequestBase base = (HttpRequestBase) pjp.getArgs()[0];
    System.out.println("Stride HTTP Execution:" + this.tokenService.getCachedStrideAccessToken());
    base.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.tokenService.getCachedStrideAccessToken());
    Map<String, Object> result = null;
    try {
      System.out.println("Stride HTTP Execution: 1");
      if (remoteUser.getRemoteUser() != null) {
        if (remoteUser.getRemoteUser().length() > 0) { //<-- verifies that the user exist, and is in the Stanford SAML System
          System.out.println("Stride HTTP Execution: 2");
          result = (Map<String, Object>) pjp.proceed();
        }
      }
      System.out.println("Stride HTTP Execution: 3");
    } catch (Throwable e) {
      logger.error("ERROR in StrideRestClient.authorizedHTTPRequest " + e.getMessage());
      throw e;
    }
    System.out.println("Stride HTTP Execution: 4" + result);
    // System.out.println("FINISH StrideAspect.authorizedHTTPRequest");
    logger.info("FINISH StrideAspect.authorizedHTTPRequest");

    return result;
  }

  @Around("execution(*  edu.stanford.biosearch.data.stride.StrideExecutor.authorizedValidatorRequest(..))")
  public Object authenticatedValidatorHttpRequest(ProceedingJoinPoint pjp) throws Throwable {
    logger.info("Stride HTTP Execution");
    System.out.println("Validator HTTP Execution");
    HttpRequestBase base = (HttpRequestBase) pjp.getArgs()[0];
    System.out.println("Validator HTTP Execution:" + this.tokenService.getCachedValidatorToken());
    base.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.tokenService.getCachedValidatorToken());
    Map<String, Object> result = null;
    try {
      System.out.println("Validator HTTP Execution: 1");
      if (remoteUser.getRemoteUser() != null) {
        if (remoteUser.getRemoteUser().length() > 0) { //<-- verifies that the user exist, and is in the Stanford SAML System
          System.out.println("Validator HTTP Execution: 2");
          result = (Map<String, Object>) pjp.proceed();
        }
      }
      System.out.println("Validator HTTP Execution: 3");
    } catch (Throwable e) {
      logger.error("ERROR in StrideRestClient.authorizedHTTPRequest " + e.getMessage());
      throw e;
    }
    if (result != null) {
      System.out.println("Validator HTTP Execution: 4" + result.toString());
    } else {
      System.out.println("Validator HTTP Execution: 4");
    }
    logger.info("FINISH StrideAspect.authorizedHTTPRequest");

    return result;
  }

}

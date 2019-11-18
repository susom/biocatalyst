package edu.stanford.integrator.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;

public class Http {
  public static String responseToString(HttpResponse response) {
    String result;

    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

      StringBuilder content = new StringBuilder();
      for (String line = rd.readLine(); line != null; line = rd.readLine()) {
        content.append(line);
      }

      result = content.toString();
    } catch (Exception e) {
      result = "";
    }

    return result;
  }
}

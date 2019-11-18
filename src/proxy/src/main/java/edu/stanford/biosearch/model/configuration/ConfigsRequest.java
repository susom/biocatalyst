package edu.stanford.biosearch.model.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.HashMap;
import java.util.Map;

public class ConfigsRequest {
  private int from = 0;
  private int size = 10000;
  private Query query;

  public int getFrom() {
    return from;
  }

  public void setFrom(int from) {
    this.from = from;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Query getQuery() {
    return query;
  }

  public void setQuery(Query query) {
    this.query = query;
  }

  // inner classes

  public class Query {
    private Bool bool;

    public Bool getBool() {
      return bool;
    }

    public void setBool(Bool bool) {
      this.bool = bool;
    }
  }

  public class Bool {
    private Map<String, Object> items = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getItems() {
      return items;
    }
  }

  public class Must {
    private Map<String, Object> items = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getItems() {
      return items;
    }
  }

  public class Should {
    private Match match;

    public Match getMatch() {
      return match;
    }

    public void setMatch(Match match) {
      this.match = match;
    }
  }

  public class Match {
    private Map<String, Object> items = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getItems() {
      return items;
    }
  }

}



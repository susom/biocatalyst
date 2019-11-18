package edu.stanford.biosearch.model.integrateddata;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import edu.stanford.biosearch.model.integrateddata.filter.Range;
import edu.stanford.biosearch.model.integrateddata.filter.Wildcard;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JsonFilter("Filter")
public class WildcardQuery {

  @JsonProperty("_source")
  private List<String> source = new LinkedList<String>();

  @JsonProperty("query")
  private Query query = new Query();

  @JsonProperty("from")
  private int from = 0;

  @JsonProperty("size")
  private int size = 0;

  @JsonProperty("sort")
  private List<Map<String, String>> sort = new LinkedList<Map<String, String>>();

  public WildcardQuery(List<String> source, Map<String, Object> filters, Map<String, String> sort, int from, int size) {
    this.source = source;

    Map<String, Object> range = (Map<String, Object>) filters.get("range");
    if (range != null) {
      for (Map.Entry<String, Object> entry : range.entrySet()) {
        Range rangeFilter = new Range();
        rangeFilter.setValue(entry.getKey(), entry.getValue());
        this.query.getBool().getMust().add(rangeFilter);
      }
    }

    Map<String, Object> match = (Map<String, Object>) filters.get("match");
    if (match != null) {
      for (Map.Entry<String, Object> entry : match.entrySet()) {
        Wildcard wildcard = new Wildcard();
        wildcard.setValue(entry.getKey(), entry.getValue());
        this.query.getBool().getMust().add(wildcard);
      }
    }

    this.from = from;
    this.size = size;
    this.sort.add(sort);

  }

  public List<String> getSource() {
    return this.source;
  }

  public Query getQuery() {
    return this.query;
  }

  public int getFrom() {
    return this.from;
  }

  public int getSize() {
    return this.size;
  }

  public List<Map<String, String>> getSort() {
    return this.sort;
  }

  public String jsonStringify() throws JsonProcessingException {
    return this.jsonStringify(null);
  }

  public String jsonStringify(SimpleBeanPropertyFilter filter) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    SimpleBeanPropertyFilter theFilter = (filter == null) ? SimpleBeanPropertyFilter.serializeAll() : filter;
    FilterProvider filters = new SimpleFilterProvider().addFilter("Filter", theFilter);

    return mapper.writer(filters).writeValueAsString(this);
  }

}

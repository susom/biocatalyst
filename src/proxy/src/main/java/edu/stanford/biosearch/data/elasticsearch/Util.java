package edu.stanford.biosearch.data.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class Util {
  private static final String RANGE = "range";
  private static final String MATCH = "match";
  private static final String NOT_MATCH = "must_not";
  private static final String PREFIX = "prefix";
  private static final String WILDCARD = "wildcard";
  private static final String REGEXP = "regexp";
  private static final String EXISTS = "exists";

  public JSONArray elasticFilter = new JSONArray();
  public JSONArray elasticMustNot = new JSONArray();

  public JSONArray elasticFilterDis_Max = new JSONArray();
  public JSONArray elasticMustNotDis_Max = new JSONArray();

  public Util() {
  }

  public JSONArray ElasticFilter() {
    if (elasticFilterDis_Max.length() > 0) {
      elasticFilter.put(new JSONObject().put("dis_max", new JSONObject().put("queries", elasticFilterDis_Max)));
    }
    return elasticFilter;
  }

  public JSONArray ElasticMustNot() {
    if (elasticMustNotDis_Max.length() > 0) {
      elasticMustNot.put(new JSONObject().put("dis_max", new JSONObject().put("queries", elasticMustNotDis_Max)));
    }
    return elasticMustNot;
  }

  public void ProcessFilters(List<Object> filters) {
      if (filters == null) {
          return;
      }
      if (filters.size() <= 0) {
          return;
      }
    for (Object obj : filters) {
      ProcessFilter((Map<String, Object>) obj);
    }
  }

  public void ProcessFilter(Map<String, Object> filter) {
    // FilterValues are the top one
    // FilterValue are the entries in the search bar
    String field = (String) filter.get("dataField");
    String dataType = (String) filter.get("dataType");
    Object filterValues = filter.get("filterValues");
    Object filterValue = filter.get("filterValue");
    String selectedFilterOperation = (String) filter.get("selectedFilterOperation");
    Object sortOrder = filter.get("sortOrder");
    Object sortIndex = filter.get("sortIndex");

    // Sanity Check
    // In this case don't create a filter
      if (filterValue == null && filterValues == null) {
          return;
      }

    //
    String queryType = this.QueryType(selectedFilterOperation);

    // Process FilterValue
    if (filterValue != null) {
      if (filterValue instanceof ArrayList) {
        // If it's a Match
        if (queryType == Util.MATCH) {
          ((ArrayList<Object>) filterValue).forEach(value -> {
            this.ConvertDevExtremeFiltersToElasticFiltersHelper(field, dataType, queryType,
                String.valueOf(value), selectedFilterOperation);
          });
        }
        if (queryType == Util.RANGE) {
          this.ConvertDevExtremeFiltersToElasticFiltersHelper(field, dataType, queryType,
              String.valueOf(((ArrayList) filterValue).get(0)),
              String.valueOf(((ArrayList) filterValue).get(1)), selectedFilterOperation, false);
        }
      } else {
        this.ConvertDevExtremeFiltersToElasticFiltersHelper(field, dataType, queryType,
            String.valueOf(filterValue), selectedFilterOperation);
      }
    }

    // Process filterValues
    if (filterValues != null) {
      if (filterValues instanceof ArrayList) {
        ((ArrayList<Object>) filterValues).forEach(value -> {
          String curatedValue = (value == null) ? null : String.valueOf(value);
          this.ConvertDevExtremeFiltersToElasticFiltersHelper(field, dataType, Util.MATCH, curatedValue,
              "=", true);
        });
      }
    }
  }

  private void ConvertDevExtremeFiltersToElasticFiltersHelper(String Field, String DataType, String QueryType,
                                                              String Value, String Operator) {
    this.ConvertDevExtremeFiltersToElasticFiltersHelper(Field, DataType, QueryType, Value, null, Operator, false);
  }

  private void ConvertDevExtremeFiltersToElasticFiltersHelper(String Field, String DataType, String QueryType,
                                                              String Value, String Operator, boolean Dis_Max) {
    this.ConvertDevExtremeFiltersToElasticFiltersHelper(Field, DataType, QueryType, Value, null, Operator, Dis_Max);
  }

  private void ConvertDevExtremeFiltersToElasticFiltersHelper(String Field, String DataType, String QueryType,
                                                              String Value, String Value2, String Operator, boolean Dis_Max) {
    JSONObject conversion = new JSONObject();

    // Check if we are search for a match null value. If so we need a special case
    // for this.
    if (QueryType == Util.MATCH && Value == null) {
      conversion = this.CreateExistQuery(Field);
        if (Dis_Max) {
            JSONObject booleanContext = new JSONObject().put("bool",
                new JSONObject().put("must_not", new JSONArray().put(conversion)));
            this.elasticFilterDis_Max.put(booleanContext);
        } else {
            this.elasticMustNot.put(conversion);
        }
      return;
    }

    // Create Json
    switch (QueryType) {
      case Util.MATCH:
        conversion = this.CreateMatchQuery(Field, Value, DataType, Operator);
          if (Dis_Max) {
              this.elasticFilterDis_Max.put(conversion);
          } else {
              this.elasticFilter.put(conversion);
          }
        break;
      case Util.NOT_MATCH:
        conversion = this.CreateMatchQuery(Field, Value, DataType, Operator);
          if (Dis_Max) {
              this.elasticMustNotDis_Max.put(conversion);
          } else {
              this.elasticMustNot.put(conversion);
          }
        break;
      case Util.RANGE:
        conversion = this.CreateRangeQuery(Field, Operator, Value, Value2);
          if (Dis_Max) {
              this.elasticFilterDis_Max.put(conversion);
          } else {
              this.elasticFilter.put(conversion);
          }
        break;
      default:
        break;
    }

  }

  private JSONObject CreateRangeQuery(String Field, String Operator, String Value, String Value2) {
    JSONObject rangeQuery = new JSONObject();
    if (Operator.equals("between")) {
      rangeQuery.put(Util.RANGE,
          new JSONObject().put(Field, new JSONObject().put("gte", Value).put("lt", Value2)));
      return rangeQuery;
    }
    // Determine Elastic Operator
    String elasticOperator = Util.ConvertRangeOperatorToElasticOperator(Operator);
    rangeQuery.put(Util.RANGE, new JSONObject().put(Field, new JSONObject().put(elasticOperator, Value)));

    return rangeQuery;
  }

  private JSONObject CreateMatchQuery(String Field, String Value, String DataType, String Operator) {
    JSONObject matchQuery = new JSONObject();
    // Behave Slightlty Differently based on Data Type.
    if (DataType != null) {
      if (DataType.equals("string")) {
        if (Operator != null) {
          switch (Operator) {
            case "contains":
              matchQuery.put(Util.MATCH, new JSONObject().put(Field, ".*" + Value + ".*"));
              break;
            case "notcontains":
              matchQuery.put(Util.REGEXP,
                  new JSONObject().put(Field.concat(".raw"), ".*" + Value + ".*"));
              break;
            case "startswith":
              matchQuery.put(Util.PREFIX, new JSONObject().put(Field.concat(".raw"), Value));
              break;
            case "endswith":
              matchQuery.put(Util.REGEXP, new JSONObject().put(Field.concat(".raw"), ".*" + Value));
              break;
            case "=":
              matchQuery.put(Util.MATCH, new JSONObject().put(Field.concat(".raw"), Value));
              break;
            case "<>":
              matchQuery.put(Util.MATCH, new JSONObject().put(Field.concat(".raw"), Value));
              break;
            default:
              matchQuery.put(Util.MATCH, new JSONObject().put(Field, Value)); // Default Value
              break;
          }
          return matchQuery;
        }
      }
    }

    matchQuery.put(Util.MATCH, new JSONObject().put(Field, Value)); // Default Value
    return matchQuery;
  }

  private JSONObject CreateExistQuery(String Field) {
    return new JSONObject().put(Util.EXISTS, new JSONObject().put("field", Field));
  }

  private String QueryType(String operator) {
    String result = Util.MATCH;
      if (operator == null) {
          return result;
      }

    switch (operator) {
      case "contains":
      case "startswith":
      case "endswith":
      case "=":
        result = Util.MATCH;
        break;
      case "notcontains":
      case "<>":// => This is "Not Equal"
        result = Util.NOT_MATCH;
        break;
      case "<":
      case ">":
      case "<=":
      case ">=":
      case "between":
        result = Util.RANGE;
        break; // => not sure about this
      default:
        result = Util.MATCH;
        break;
    }
    return result;
  }

  private static String ConvertRangeOperatorToElasticOperator(String operator) {
    String result = null;
    switch (operator) {
      case "<":
        result = "lt";
        break;
      case ">":
        result = "gt";
        break;
      case "<=":
        result = "lte";
        break;
      case ">=":
        result = "gte";
        break;
      default:
        break;
    }
    return result;
  }

}

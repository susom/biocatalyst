package edu.stanford.biosearch.data.elasticsearch;

import edu.stanford.biosearch.model.integrateddata.BasicQuery;
import edu.stanford.biosearch.model.integrateddata.WildcardQuery;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntegrationClient {
  private static final Logger logger = Logger.getLogger(IntegrationClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  public Map<String, Object> getMultiIndexIntegratedData(BasicQuery basicQuery) throws IOException {
    List<String> indexes = basicQuery.getIndices();

    StringBuilder flattenedIndexes = new StringBuilder();
    for (int index = 0; index < indexes.size(); index++) {
      // Store found indexes
      if (flattenedIndexes.length() > 0) {
        flattenedIndexes.append(",");
      }
      flattenedIndexes.append(indexes.get(index));
    }

    // Continue Search
    System.out.println(flattenedIndexes.toString());
    return this.getIntegratedData(flattenedIndexes.toString(), basicQuery, true);
  }

  public Map<String, Object> getIntegratedData(String index, BasicQuery basicQuery) throws IOException {
    return this.getIntegratedData(index, basicQuery, false);
  }

  public Map<String, Object> getIntegratedData(String index, BasicQuery basicQuery, boolean ignore_unavailable)
      throws IOException {
    String elasticUrl = this.elasticNetwork.elasticsearchUrl.concat("/").concat(index).concat("/").concat("_search");
    if (ignore_unavailable) {
      elasticUrl = elasticUrl.concat("?ignore_unavailable=true");
    }

    WildcardQuery query = new WildcardQuery(basicQuery.getColumns(), basicQuery.getFilters(), basicQuery.getSort(),
        basicQuery.getFrom(), basicQuery.getSize());

    return this.elasticNetwork.post(elasticUrl, query.jsonStringify());
  }

  public Map<String, Object> globalSearch(String searchQuery, List<String> indices, int size, int from,
                                          boolean aggregations, boolean highlights, List<Object> filters, String suggestionfield, List<String> sources) throws IOException {

    StringBuilder flatIndex = new StringBuilder();
    for (String index : indices) {
      flatIndex.append(index);
      flatIndex.append(",");
    }

    String url = this.elasticNetwork.elasticsearchUrl.concat("/").concat(flatIndex.toString()).concat("/").concat("_search")
        .concat("?ignore_unavailable=true");

    JSONObject obj = new JSONObject();
    if (sources.size() > 0) {
      JSONArray source = new JSONArray();
      for (String _source : sources) {
        source.put(_source);
      }
      obj.put("_source", source);
    }

    JSONObject aggs = new JSONObject();
    if (aggregations) {
      aggs.put("indices", new JSONObject()
          .put("composite", new JSONObject()
              .put("size", 100)
              .put("after", new JSONObject()
                  .put("index", ""))
              .put("sources", new JSONArray()
                  .put(new JSONObject()
                      .put("index", new JSONObject()
                          .put("terms", new JSONObject()
                              .put("field", "_index")))))));
    }
    if (suggestionfield.length() > 0) {
      aggs.put("suggested_terms", new JSONObject()
          .put("terms", new JSONObject()
              .put("field", suggestionfield + ".raw")));
    }

    obj.put("aggs", aggs);

    if (suggestionfield.length() > 0) {

      obj.put("suggest", new JSONObject()
          .put("text", searchQuery)
          .put("simple_phrase", new JSONObject()
              .put("phrase", new JSONObject()
                  .put("field", suggestionfield)
                  .put("size", 5)
                  .put("max_errors", "2")
                  .put("direct_generator", new JSONArray()
                          .put(new JSONObject()
                              .put("field", suggestionfield)
                              .put("suggest_mode", "always")
                              .put("min_word_length", 1))
                                            /*
                                            .put(new JSONObject()
                                                    .put("field", suggestionfield)
                                                    .put("suggest_mode", "always")
                                                    .put("pre_filter", "reverse")
                                                    .put("post_filter", "revers"))*/)

                  .put("collate", new JSONObject()
                      .put("query", new JSONObject()
                          .put("source", new JSONObject()
                              .put("match", new JSONObject()
                                  .put("{{field_name}}", "{{suggestion}}"))))
                      .put("params", new JSONObject().put("field_name", suggestionfield))
                      .put("prune", true)))));
    }

    if (highlights) {
      obj.put("highlight", new JSONObject()
          .put("fields", new JSONObject()
              .put("*", new JSONObject())
          )
          .put("pre_tags", "<b>")
          .put("post_tags", "</b>")
      );
    }

    obj.put("from", from);
    obj.put("size", size);

    // Create Filter Entries
    Util temp = new Util();
    temp.ProcessFilters(filters);
    JSONArray filterArray = temp.ElasticFilter();
    JSONArray mustNotArray = temp.ElasticMustNot();
        /*
        for(Map<String,Object> filter : filters) {
            for(Map.Entry<String,Object> entry : filter.entrySet() ) {
                filterArray.put(new JSONObject()
                        .put("match", new JSONObject()
                                .put(entry.getKey(),entry.getValue())
                        )
                );      
            }
        }
        */

    // Create Query Entries
    JSONArray queryArray = new JSONArray();
    if (searchQuery.length() > 0) {
      queryArray
          .put(new JSONObject()
              .put("multi_match", new JSONObject()
                  .put("analyzer", "standard")
                  .put("query", searchQuery)
                  .put("type", "most_fields")
                  .put("fuzziness", "AUTO")
              )
          )
          .put(new JSONObject()
              .put("multi_match", new JSONObject()
                  .put("analyzer", "whitespace")
                  .put("query", searchQuery)
                  .put("type", "most_fields")
                  .put("fuzziness", "AUTO")
              )
          );
    }

    obj.put("query", new JSONObject()
        .put("bool", new JSONObject()
            .put("must_not", mustNotArray)
            .put("filter", filterArray)
            .put("should", queryArray))
    );

    return this.elasticNetwork.post(url, obj.toString());
  }
}

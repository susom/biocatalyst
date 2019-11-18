package edu.stanford.biosearch.data.elasticsearch;

import java.io.IOException;
import java.util.Map;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompletionSuggestionClient {
  private static final Logger logger = Logger.getLogger(CompletionSuggestionClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  public Map<String, Object> getCompletionSuggestion(String index, String field, String prefix) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat("/").concat(index).concat(this.elasticNetwork.suggestionPath);

    HttpPost post = new HttpPost(url);

    JSONObject fieldJson = new JSONObject();
    fieldJson.put("field", field);

    JSONObject compJson = new JSONObject();
    compJson.put("completion", fieldJson);
    compJson.put("prefix", prefix);

    JSONObject compSuggestJson = new JSONObject();
    compSuggestJson.put("completion_suggest", compJson);

    JSONObject root = new JSONObject();
    root.put("_source", false);
    root.put("suggest", compSuggestJson);
    int size = 10;
    JSONObject obj = new JSONObject();
    obj.put("from", 0);
    obj.put("size", 0);

    obj.put("aggs", new JSONObject()
        .put("suggested_terms", new JSONObject()
            .put("aggs", new JSONObject()
                .put("max_score", new JSONObject()
                    .put("max", new JSONObject()
                        .put("script", "_score"))))
            .put("terms", new JSONObject()
                .put("field", field + ".raw")
                .put("order", new JSONObject()
                    .put("max_score", "desc")))));

    if (field.length() > 0) {
      obj.put("query", new JSONObject()
          .put("bool", new JSONObject()
              .put("should", new JSONArray()
                  .put(new JSONObject()
                      .put("match", new JSONObject()
                          .put(field, new JSONObject()
                              .put("query", prefix))))
                  .put(new JSONObject()
                      .put("match", new JSONObject()
                          .put(field + ".ngram", new JSONObject()
                              .put("query", prefix))))
                  .put(new JSONObject()
                      .put("prefix", new JSONObject()
                          .put(field + ".ngram", prefix))))));
    }

    obj.put("suggest", new JSONObject()
        .put("text", prefix)
        .put("simple_phrase", new JSONObject()
            .put("phrase", new JSONObject()
                .put("field", field)
                .put("size", size)
                .put("max_errors", "2")
                .put("direct_generator", new JSONArray()
                    .put(new JSONObject()
                        .put("field", field)
                        .put("suggest_mode", "always")
                        .put("min_word_length", 1)))
                .put("collate", new JSONObject()
                    .put("query", new JSONObject()
                        .put("source", new JSONObject()
                            .put("match", new JSONObject()
                                .put("{{field_name}}", "{{suggestion}}"))))
                    .put("params", new JSONObject().put("field_name", field))
                    .put("prune", true)))));

    logger.info(root.toString());

    return this.elasticNetwork.post(url, obj.toString());
  }
}

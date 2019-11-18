package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import edu.stanford.biosearch.model.integrateddata.BasicQuery;
import edu.stanford.biosearch.model.integrateddata.ElasticQuery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrollClient {
  private static final Logger logger = Logger.getLogger(ScrollClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  public Map<String, Object> scrollQuery(String index, int chunkSize, BasicQuery basicQuery) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat("/").concat(index).concat("/").concat("_search").concat("?scroll=10s");

    SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("from");

    ElasticQuery query = new ElasticQuery(basicQuery.getColumns(), basicQuery.getFilters(), basicQuery.getSort(),
        basicQuery.getFrom(), chunkSize);

    System.out.println(query.jsonStringify(filter));

    return this.elasticNetwork.post(url, query.jsonStringify(filter));
  }

  // Elastic Continue Scrolling
  public Map<String, Object> continueScrollQuery(String scroll_id) throws IOException {
    return this.continueScrollQuery(scroll_id, "10s");
  }

  // Elastic Continue Scrolling
  public Map<String, Object> continueScrollQuery(String scroll_id, String scroll_window) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat("/").concat("_search").concat("/").concat("scroll");

    JSONObject compJson = new JSONObject();
    compJson.put("scroll", scroll_window);
    compJson.put("scroll_id", scroll_id);

    System.out.println(compJson.toString());

    return this.elasticNetwork.post(url, compJson.toString());
  }

  // Elastic Process Scrolling
  public String ProccessScroll(Map<String, Object> scroll, LinkedList<Object> data) throws Exception {
    Map<String, Object> responseBody = (Map<String, Object>) scroll.get("responseBody");
    String scroll_id = (String) responseBody.get("_scroll_id");
    Map<String, Object> hits = (Map<String, Object>) responseBody.get("hits");
    ArrayList<Object> hitList = (ArrayList<Object>) hits.get("hits");

    // Add to data
    data.addAll(hitList);

    // Continue/Delete if Neccessary
    if (hitList.size() > 0) {
      // Map<String, Object> result2 = client.continueScrollQuery(scroll_id);
      return scroll_id;
    }

    // Delete Scrolling list.
    System.out.println(this.deleteScroll(scroll_id));
    return null;
  }

  // Delete Document Scroll
  public Map<String, Object> deleteScroll(String scroll_id) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl.concat("/").concat("_search").concat("/").concat("scroll").concat("/")
        .concat(scroll_id);
    return this.elasticNetwork.delete(url);
  }

  // Elastic Process Scrolling
  // To be used in conjunction with processing a scroll. It will Extract the
  // source from data.
  public void ProccessScrollingSourceOnly(LinkedList<Object> list) throws Exception {
    for (int index = 0; index < list.size(); ++index) {
      Map<String, Object> data = (Map<String, Object>) list.get(index);
      list.set(index, data.get("_source"));
    }
  }

}

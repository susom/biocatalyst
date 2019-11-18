package edu.stanford.biosearch.data.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.model.bookmark.Bookmark;
import edu.stanford.biosearch.model.integrateddata.ElasticQuery;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookmarkClient {
  private static final Logger logger = Logger.getLogger(BookmarkClient.class);

  @Autowired
  private HttpExecutor httpExecutor;

  @Autowired
  private NetworkClient elasticNetwork;

  // Elastic Create Bookmark
  public Map<String, Object> createBookmark(Bookmark bookmark) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl
        .concat("/").concat("reports")
        .concat("/").concat("doc");

    ObjectMapper mapper = new ObjectMapper();
    String bookmarkJson = mapper.writeValueAsString(bookmark);

    return this.elasticNetwork.post(url, bookmarkJson);
  }

  // Elastic Get Bookmarks
  public Map<String, Object> getBookmarks() throws IOException {
    String elasticUrl = this.elasticNetwork.elasticsearchUrl
        .concat("/").concat("reports")
        .concat("/").concat("_search");

    Map<String, Object> filters = new HashMap<String, Object>();

    ElasticQuery query = new ElasticQuery(new LinkedList<>(), filters, new HashMap<String, String>(), 0, 10000);

    System.out.println(query.jsonStringify());
    System.out.println(filters);
    return this.elasticNetwork.post(elasticUrl, query.jsonStringify());
  }

  // Elastic Update Bookmark
  public Map<String, Object> UpdateBookmark(String ID, Bookmark bookmark) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl
        .concat("/").concat("reports")
        .concat("/").concat("doc")
        .concat("/").concat(ID);

    ObjectMapper mapper = new ObjectMapper();
    String bookmarkJson = mapper.writeValueAsString(bookmark);

    return this.elasticNetwork.post(url, bookmarkJson);
  }

  // Elastic Delete Bookmark
  public Map<String, Object> deleteBookmark(String document) throws IOException {
    String url = this.elasticNetwork.elasticsearchUrl
        .concat("/").concat("reports")
        .concat("/").concat("doc")
        .concat("/").concat(document);
    return this.elasticNetwork.delete(url);
  }

  // Elastic Bookmark Exists
  public Map<String, Object> BookmarkExists(String id) throws IOException {
    return this.elasticNetwork.DocumentExists("reports", "doc", id);
  }
}

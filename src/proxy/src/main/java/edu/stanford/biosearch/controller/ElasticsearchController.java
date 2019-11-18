package edu.stanford.biosearch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.biosearch.model.SuggestionRequest;
import edu.stanford.biosearch.model.bookmark.Bookmark;
import edu.stanford.biosearch.model.configuration.DataSet;
import edu.stanford.biosearch.model.configuration.DataSource;
import edu.stanford.biosearch.model.globalsearchrequest.GlobalSearchRequest;
import edu.stanford.biosearch.model.integrateddata.BasicQuery;
import edu.stanford.biosearch.service.ElasticsearchService;
import edu.stanford.biosearch.util.StringJsonToClass;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ElasticsearchController {
  private static final Logger logger = Logger.getLogger(ElasticsearchController.class);

  @Autowired
  ElasticsearchService elasticsearchService;

  @Autowired
  StringJsonToClass stringJsonToClass;

  @RequestMapping(method = RequestMethod.POST, value = "/suggestion")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getCompletionSuggestion(@RequestBody SuggestionRequest request) throws Exception {
    return elasticsearchService.getCompletionSuggestion(request.getIndex(), request.getField(), request.getQuery());
  }

  @RequestMapping(method = RequestMethod.GET, value = "/distinct-column-values/{indexname}/{column}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getDistinctColumnValues(@PathVariable("indexname") String indexname,
                                                     @PathVariable("column") String column) throws Exception {
    return elasticsearchService.getDistinctColumnValues(indexname, column);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/configuration")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Map<String, Object> createConfig(@RequestBody DataSet dataSet) throws Exception {
    List<DataSource> dataSources = dataSet.getDataSources();
    if (dataSources.size() < 1) {
      throw new IllegalArgumentException("At least 1 Data Source is required.");
    }

    return elasticsearchService.createConfig(dataSet);
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/configuration/{document}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> updateConfig(@PathVariable("document") String document, @RequestBody DataSet dataSet) throws Exception {
    return elasticsearchService.updateConfig(document, dataSet);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/configuration")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getConfigs(@RequestParam(value = "config", required = false) List<String> configs)
      throws Exception {
    return elasticsearchService.getConfigs(configs);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/configuration/{document}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> deleteConfig(@PathVariable("document") String document) throws Exception {
    return elasticsearchService.deleteConfiguration(document);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/integration/{index}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getIntegration(@PathVariable("index") String index,
                                            @RequestBody(required = false) String requestBody,
                                            @RequestParam(value = "mapping", required = false) boolean mapping,
                                            @RequestParam(value = "data", required = true) boolean data) throws Exception {

    BasicQuery query = null;
    if (data) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(requestBody);
      query = mapper.treeToValue(jsonNode, BasicQuery.class);
    }
    // Validations Happen Here

    return elasticsearchService.getIntegrationAndMapping(index, mapping, query, data);
  }

  // Bookmarks - Multi Index Search Query
  @RequestMapping(method = RequestMethod.POST, value = "/integration")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> search(@RequestBody String query) throws Exception {
    BasicQuery basicQuery = stringJsonToClass.convert(query, BasicQuery.class);
    List<String> indices = basicQuery.getIndices();

    if (indices.size() > 0) {
      throw new IllegalArgumentException("Cannot specify Indices. Zero Indicies requred.");
    }

    return elasticsearchService.searchAcrossIntegrations(basicQuery);
  }

  // Bookmarks - Get
  @RequestMapping(method = RequestMethod.GET, value = "/bookmarks")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getBookmarks() throws Exception {
    return elasticsearchService.getBookmarks();
  }

  // Bookmarks - Create
  @RequestMapping(method = RequestMethod.POST, value = "/bookmarks")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Map<String, Object> createBookmarks(@RequestBody String bookmark) throws Exception {
    Bookmark mark = stringJsonToClass.convert(bookmark, Bookmark.class);
    return elasticsearchService.createBookmark(mark);
  }

  // Bookmarks - Update
  @RequestMapping(method = RequestMethod.PUT, value = "/bookmarks/{bookmarkid}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> updateBookmarks(@RequestBody String bookmark, @PathVariable("bookmarkid") String id)
      throws Exception {
    Bookmark mark = stringJsonToClass.convert(bookmark, Bookmark.class);

    if (mark.getID() == null) {
      throw new IllegalArgumentException("Bookmark doesn't contain an ID");
    }

    if (!mark.getID().equals(id)) {
      throw new IllegalArgumentException("ID Mismatch: Bookmark ID doesn't match given ID");
    }

    return elasticsearchService.updateBookmark(id, mark);
  }

  // Bookmarks - Delete
  @RequestMapping(method = RequestMethod.DELETE, value = "/bookmarks/{bookmarkid}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> deleteBookmarks(@PathVariable("bookmarkid") String id) throws Exception {
    return elasticsearchService.deleteBookmark(id);
  }

  // Bookmarks - Download Query
  @RequestMapping(method = RequestMethod.POST, value = "/download/{index}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> downloadQuery(@PathVariable("index") String index, @RequestBody String query)
      throws Exception {
    BasicQuery basicQuery = stringJsonToClass.convert(query, BasicQuery.class);

    if (index.equals("users") || index.equals("reports") || index.equals("integrations")) {
      throw new IllegalArgumentException("Index Unavailable");
    }

    return elasticsearchService.downloadQuery(index, basicQuery);
  }

  /**
   * Accepts an integration index and a role. It adds the index to the role with
   * read/write. It also adds the entry in /integrations to the role with
   * readonly. The index name is the same as the _index field in each hit in
   * /integrations.
   *
   * @param data
   * @return
   * @throws IOException
   */
  @RequestMapping(value = "/elastic/role", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> addIndexToRole(@RequestBody Map<String, String> data) throws IOException {
    return elasticsearchService.addIndexToRole(data.get("index"), data.get("role"));
  }

  /**
   * Accepts an integration index and a role. It adds the index to the role with
   * read/write. It also adds the entry in /integrations to the role with
   * readonly. The index name is the same as the _index field in each hit in
   * /integrations.
   *
   * @param data
   * @return
   * @throws IOException
   */
  @RequestMapping(value = "/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> GlobalSearch(@RequestBody String body) throws Exception {

    GlobalSearchRequest request = stringJsonToClass.convert(body, GlobalSearchRequest.class);

    return elasticsearchService.globalSearch(request.getQuery(), request.getIndices(), request.getSize(),
        request.getFrom(), request.getAggregations(), request.getHighlights(), request.getIndexMappings(),
        request.getIndexNames(), request.getFilters(), request.getSuggestionfield(), request.getSources());
  }

  /**
   * Accepts an integration index and a role. It adds the index to the role with
   * read/write. It also adds the entry in /integrations to the role with
   * readonly. The index name is the same as the _index field in each hit in
   * /integrations.
   *
   * @param data
   * @return
   * @throws IOException
   */
  @RequestMapping(value = "/headers/{index}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Object> getHeaders(@PathVariable() String index) throws Exception {
    return elasticsearchService.getMapping(index);
  }
}

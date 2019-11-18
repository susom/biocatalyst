package edu.stanford.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Indexer {
  private Logger logger = Logger.getLogger(Indexer.class.getName());

  @Autowired
  Configuration configuration;

  private File CONFIG_FILE_DIR;

  private String ELASTIC_URL;

  private static final String BASE_CONF_PREFIX = "logstash-";
  private static final String POSTPROCESSING_CONF_PREFIX = "postprocessing-";
  private static final String FINISHEDFLAG_PREFIX = "finishedflag-";

  private final Map<String, IndexRequest> indexRequestMap;
  public final LinkedList<IndexRequest> indexRequestQueue;

  String addIndexRequest(IndexRequest indexRequest) {
    String id = indexRequest.getIndexName();
    indexRequestMap.put(id, indexRequest);
    indexRequestQueue.add(indexRequest);
    return id;
  }

  IndexRequest removeIndexRequest(String id) {
    return indexRequestMap.remove(id);
  }

  IndexRequest getIndexRequest(String id) {
    return indexRequestMap.get(id);
  }

  public Indexer() {
    indexRequestMap = new HashMap<>();
    indexRequestQueue = new LinkedList<>();
  }

  @PostConstruct
  public void init() {
    ELASTIC_URL = configuration.ELASTIC_PROTCOL + "://" + configuration.ELASTIC_HOST + ":" + configuration.ELASTIC_PORT;
    logger.log(Level.INFO, "Indexer will be using elasticsearch at:" + ELASTIC_URL);
    CONFIG_FILE_DIR = new File(configuration.INDEXER_CONFIG_FILE_ROOT_DIR);
  }

  public void executeIndex(IndexRequest indexRequest) {
    indexRequest.setStatusAndResult(Status.WAITING, "");

    String indexName = indexRequest.getIndexName();
    String sqlQuery = indexRequest.getSql();
    String pipelineId = indexRequest.getPipelineId();
    boolean cleanReindex = indexRequest.isCleanIndex();

    logger.log(Level.INFO, "Starting to execute a reindex for " + indexName);
    String completionSuggestionResult = "";
    try {
      logger.log(Level.INFO, "Registering pipelineId: " + pipelineId);

      createTempScripts(indexName, sqlQuery, pipelineId, cleanReindex);
    } catch (Exception e) {
      String errorMsg = "Error while creating logstash configuration scripts for " + indexName;
      indexRequest.setStatusAndResult(Status.ERROR, errorMsg + "\n" + e.getMessage());
    }
    try {
      if (cleanReindex) {
        deleteIndex(indexName);
      }
    } catch (Exception e) {
      String errorMsg = "Error while deleting index " + indexName;
      indexRequest.setStatusAndResult(Status.ERROR, errorMsg + "\n" + e.getMessage());
    }
    try {
      if (!indexExists(indexName)) {
        completionSuggestionResult = createCompletionSuggestionIndex(indexName) + " ";
      }
    } catch (Exception e) {
      String errorMsg = "Error while creating completion suggestion for " + indexName;
      indexRequest.setStatusAndResult(Status.ERROR, errorMsg + "\n" + e.getMessage());
    }

    try {
      final String baseDir = CONFIG_FILE_DIR.getAbsolutePath()
          + "/pipeline" + pipelineId;
      final String baseConf = baseDir
          + "/" + BASE_CONF_PREFIX + indexName + ".conf";
      final String postprocessingConf = baseDir
          + "/" + POSTPROCESSING_CONF_PREFIX + indexName + ".conf";
      final String finishedflagConf = baseDir
          + "/" + FINISHEDFLAG_PREFIX + indexName;

      // poll for finished flag to indicate that it's done work
      WatchService watchService = FileSystems.getDefault().newWatchService();

      // Wait until finishedflag conf file appears, then delete and signal success
      Path path = Paths.get(baseDir);
      logger.log(Level.INFO, "Registering path: " + baseDir);
      try {
        WatchKey pathKey = path.register(
            watchService, StandardWatchEventKinds.ENTRY_CREATE);
      } catch (Exception ex) {
        logger.log(Level.INFO, "Caught exception: " + ex);
      }

      while (true) {
        logger.log(Level.INFO, "Waiting for finishedflag-" + indexName);

        WatchKey key;
        try {
          key = watchService.take();
        } catch (InterruptedException x) {
          return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent<Path> ev = (WatchEvent<Path>) event;
          Path filename = ev.context();
          if (filename.toString().equals(FINISHEDFLAG_PREFIX + indexName)) {
            logger.log(Level.INFO, "WatchEvent saw finish flag for integration: " + indexName + " was created!");
          }
        }
        break;
      }

      File base = new File(baseConf);
      File postprocessing = new File(postprocessingConf);
      File finishedflag = new File(finishedflagConf);

      // TODO: Add another watcher to wait until finishedflag is no longer being written to, then we're ready to delete. For now, sleep
      logger.log(Level.INFO, "Sleeping 70 seconds to finish everything. See TODO in Indexer.java!");
      Thread.sleep(70000);

      try {
        base.delete();
        postprocessing.delete();
        finishedflag.delete();
      } catch (Exception ex) {
        logger.log(Level.INFO, "Failed to cleanup some config files for index: " + indexName + ", "
            + "with exception: " + ex);

      }

      // set status to done
      indexRequest.setStatusAndResult(Status.SUCCESS, "Successfully completed indexing deleted all logstash confs associated with index: " + indexName);

    } catch (Exception e) {
      String errorMsg = "Error while indexing " + indexName;
      indexRequest.setStatusAndResult(Status.ERROR, errorMsg + "\n" + e.getMessage());
    }
  }

  public void createTempScripts(String indexName, String sqlQuery, String pipelineId, boolean cleanReindex)
      throws FileNotFoundException, IOException, Exception {
    // Elasticsearch doesn't deal with uppercase indices
    if (!indexName.equals(indexName.toLowerCase())) {
      throw new Exception("index name must be all lower-case. Got: " + indexName);
    }

    logger.log(Level.INFO, "Creating temp logstash config for " + indexName + " using: " + sqlQuery + " , for a cleanReindex=" + cleanReindex);

    String baseFilename = CONFIG_FILE_DIR.getAbsolutePath()
        + "/pipeline" + pipelineId
        + "/" + BASE_CONF_PREFIX + indexName + ".conf";
    String postprocessingFilename = CONFIG_FILE_DIR.getAbsolutePath()
        + "/pipeline" + pipelineId
        + "/" + POSTPROCESSING_CONF_PREFIX + indexName + ".conf";

    File baseFile;
    File postProcessingFile;
    if (sqlQuery == null) { //add documentation
      throw new Exception("Exception: SQL query is null");
    } else {
      baseFile = createBaseConfigFile(baseFilename, indexName, sqlQuery, pipelineId);
      postProcessingFile = createPostProcessingConfigFile(postprocessingFilename, indexName, pipelineId);
    }
  }

  public String[] getAllIndexNames() throws Exception {
    File[] confFiles = CONFIG_FILE_DIR.listFiles();
    String[] indexNames = new String[confFiles.length];
    for (int i = 0; i < confFiles.length; i++) {
      String fileName = confFiles[i].getName();
      int period_pos = fileName.lastIndexOf(".");
      int start_pos = BASE_CONF_PREFIX.length();
      if (period_pos > 0) {
        indexNames[i] = fileName.substring(start_pos, period_pos);
      } else {
        throw new Exception("Error: Configuration file has no extension");
      }
    }
    return indexNames;
  }

  private File createBaseConfigFile(String filename, String indexName, String sql_query, String pipelineId) throws IOException {
    sql_query = sql_query.replaceAll("\"", "\\\\\"");

    final String BASE_CONFIG_STRING = "input {\n"
        + "   jdbc {\n"
        + "        jdbc_validate_connection => true\n"
        + "        jdbc_connection_string => \"%s\"\n"
        + "        jdbc_user => \"" + configuration.DATALAKE_DB_USER + "\"\n"
        + "        jdbc_password => \"" + configuration.DATALAKE_DB_PASSWORD + "\"\n"
        + "        jdbc_driver_library => \"" + configuration.JDBC_DRIVERLIBRARY + "\"\n"
        + "        jdbc_driver_class => \"" + configuration.JDBC_DRIVER_CLASS + "\"\n"
        + "        statement => \"%s\"\n"
        + "       }\n"
        + "}\n"
        + "output   {\n"
        + "    elasticsearch {\n"
        + "        index => \"%s\"\n"
        + "        hosts => [\"%s\"]\n"
        + "        user => %s\n"
        + "        password => %s\n"
        + "        ssl => %s\n"
        + "        document_id => \"%%{[%s]}\"\n"
        + "    }\n"
        + "    pipeline { send_to => [postProcessing%s] }\n"
        + "}";

    logger.log(Level.INFO, "Creating a base config file: " + filename);

    File configFile = new File(filename);
    if (configFile.exists()) {
      configFile.delete();
    }
    configFile.createNewFile();

     // jdbc:postgresql://db:5432/biocatalyst
    String DB_URL = "jdbc:postgresql://" + configuration.DATALAKE_DB_HOST + ":" + configuration.DATALAKE_DB_PORT + "/"
            + configuration.DATALAKE_DB_NAME;
    logger.log(Level.INFO, "JDBC Connection string: " + DB_URL);

    String configString = String.format(BASE_CONFIG_STRING,
        DB_URL,
        sql_query,
        indexName,
        configuration.ELASTIC_HOST_DOCKER + ":" + configuration.ELASTIC_PORT,
        configuration.ELASTIC_USER,
        configuration.ELASTIC_PWD,
        configuration.ELASTIC_SSL_ON,
        configuration.INDEXER_DOCUMENT_ID_FIELD.toLowerCase(), // Logstash uses this field, and is set to work in lowercase only mode
        pipelineId
    );

    BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
    bw.write(configString);
    bw.close();

    return configFile;
  }

  public File createPostProcessingConfigFile(String filename, String indexName, String pipelineId) throws IOException {
    final String POSTPROCESSING_CONFIG_STRING = "input {\n" +
            "  pipeline { address => postProcessing%s }\n" +
            "}\n" +
            "output {\n" +
            "  file{\n" +
            "    path => \"%s/finishedflag-%s\"\n" +
            "    codec => \"dots\"\n" +
            "  }\n" +
            "}";

    logger.log(Level.INFO, "Creating a postprocessing config file: " + filename);

    File configFile = new File(filename);
    if (configFile.exists()) {
      configFile.delete();
    }
    configFile.createNewFile();

    String configString = String.format(POSTPROCESSING_CONFIG_STRING,
            pipelineId,
            "/pipelines/pipeline" + pipelineId, //TODO make a property
            indexName
    );

    BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
    bw.write(configString);
    bw.close();

    return configFile;
  }

  public String createCompletionSuggestionIndex(String indexName)
      throws Exception, IOException {

    // Elastisearch doesn't deal with uppercase indecies
    if (indexName.equals(indexName.toLowerCase()) == false) {
      throw new Exception("index name must be all lower-case. Got: " + indexName);
    }
    JSONObject configuration = new JSONObject();
    configuration.put("settings", new JSONObject()
        .put("analysis", new JSONObject()
            .put("analyzer", new JSONObject()
                .put("ngram", new JSONObject()
                    .put("type", "custom")
                    .put("tokenizer", "ngram_tokenizer")
                    .put("filter", new JSONArray()
                        .put("lowercase"))))
            .put("tokenizer", new JSONObject()
                .put("ngram_tokenizer", new JSONObject()
                    .put("type", "ngram")
                    .put("min_grap", 3)
                    .put("max_gram", 20)
                    .put("token_chars", new JSONArray()
                        .put("letter")
                        .put("digit"))))));
    configuration.put("mappings", new JSONObject()
        .put("doc", new JSONObject()
            .put("date_detection", "true")
            .put("numeric_detection", "true")
            .put("dynamic_date_formats", new JSONArray()
                .put("MM/dd/yyyy")
                .put("dd/MM/yyyy")
                .put("yyyy/MM/dd")
                .put("yyyy/dd/MM")
                .put("MM/dd/yyyy HH")
                .put("dd/MM/yyyy HH")
                .put("yyyy/MM/dd HH")
                .put("yyyy/dd/MM HH")
                .put("MM/dd/yyyy HH:mm")
                .put("dd/MM/yyyy HH:mm")
                .put("yyyy/MM/dd HH:mm")
                .put("yyyy/dd/MM HH:mm")
                .put("MM/dd/yyyy HH:mm:ss")
                .put("dd/MM/yyyy HH:mm:ss")
                .put("yyyy/MM/dd HH:mm:ss")
                .put("yyyy/dd/MM HH:mm:ss")
                .put("MM-dd-yyyy")
                .put("dd-MM-yyyy")
                .put("yyyy-MM-dd")
                .put("yyyy-dd-MM")
                .put("MM-dd-yyyy HH")
                .put("dd-MM-yyyy HH")
                .put("yyyy-MM-dd HH")
                .put("yyyy-dd-MM HH")
                .put("MM-dd-yyyy HH:mm")
                .put("dd-MM-yyyy HH:mm")
                .put("yyyy-MM-dd HH:mm")
                .put("yyyy-dd-MM HH:mm")
                .put("MM-dd-yyyy HH:mm:ss")
                .put("dd-MM-yyyy HH:mm:ss")
                .put("yyyy-MM-dd HH:mm:ss")
                .put("yyyy-dd-MM HH:mm:ss"))
            .put("dynamic_templates", new JSONArray()
                .put(new JSONObject()
                    .put("IgnoreJoinId", new JSONObject()
                        .put("match_pattern", "regex")
                        .put("match", ".*_join_id_.*")
                        .put("mapping", new JSONObject()
                            .put("index", false)
                        )))
                .put(new JSONObject()
                    .put("IgnoreJoinVisitIds", new JSONObject()
                        .put("match_pattern", "regex")
                        .put("match", ".*_join_visit_.*")
                        .put("mapping", new JSONObject()
                            .put("index", false)
                        )))
                .put(new JSONObject()
                    .put("IgnoreKeyWords", new JSONObject()
                        .put("match_pattern", "regex")
                        .put("match", "^rownumber$|^@version$|^@timestamp$|^s\\d+_datasource__import__index$")
                        .put("mapping", new JSONObject()
                            .put("index", false)
                        )))
                .put(new JSONObject()
                    .put("Raw:Boolean", new JSONObject()
                        .put("mapping", new JSONObject()
                            .put("analyzer", "standard")
                            .put("fields", new JSONObject()
                                .put("raw", new JSONObject()
                                    .put("ignore_above", 256)
                                    .put("type", "keyword")))
                            .put("type", "boolean"))
                        .put("match_mapping_type", "boolean")))
                .put(new JSONObject()
                    .put("Raw:Date", new JSONObject()
                        .put("mapping", new JSONObject()
                            .put("analyzer", "standard")
                            .put("fields", new JSONObject()
                                .put("raw", new JSONObject()
                                    .put("ignore_above", 256)
                                    .put("type", "keyword")))
                            .put("type", "date"))
                        .put("match_mapping_type", "date")))
                .put(new JSONObject()
                    .put("Raw:Double", new JSONObject()
                        .put("mapping", new JSONObject()
                            .put("analyzer", "standard")
                            .put("fields", new JSONObject()
                                .put("raw", new JSONObject()
                                    .put("ignore_above", 256)
                                    .put("type", "keyword")))
                            .put("type", "double"))
                        .put("match_mapping_type", "double")))
                .put(new JSONObject()
                    .put("Raw:Long", new JSONObject()
                        .put("mapping", new JSONObject()
                            .put("analyzer", "standard")
                            .put("fields", new JSONObject()
                                .put("raw", new JSONObject()
                                    .put("ignore_above", 256)
                                    .put("type", "keyword")))
                            .put("type", "long"))
                        .put("match_mapping_type", "long")))
                .put(new JSONObject()
                    .put("Raw:String", new JSONObject()
                        .put("mapping", new JSONObject()
                            .put("analyzer", "standard")
                            .put("fields", new JSONObject()
                                .put("raw", new JSONObject()
                                    .put("ignore_above", 256)
                                    .put("type", "keyword"))
                                .put("whitespace", new JSONObject()
                                    .put("analyzer", "whitespace")
                                    .put("type", "text"))
                                .put("ngram", new JSONObject()
                                    .put("analyzer", "ngram")
                                    .put("type", "text")))
                            .put("term_vector", "with_positions_offsets")
                            .put("type", "text"))
                        .put("match_mapping_type", "string"))))
            .put("properties", new JSONObject())));

    logger.log(Level.INFO, "Creating Mapping" + indexName, configuration.toString());

    String response = executePut(ELASTIC_URL + "/" + indexName, configuration.toString());
    logger.log(Level.INFO, "Requesting Elasticsearch to create an autocompletion suggestions index. Elasticsearch responded with: " + response);
    return response;
  }

  private int deleteIndex(String indexName) throws Exception, IOException {
    // Elastisearch doesn't deal with uppercase indecies
    if (!indexName.equals(indexName.toLowerCase())) {
      throw new Exception("index name must be all lower-case. Got: " + indexName);
    }

    logger.log(Level.INFO, "Deleting index: " + indexName);
    int return_status = executeDelete(ELASTIC_URL + "/" + indexName);
    return return_status;
  }

  private boolean indexExists(String indexName) throws Exception, IOException {
    // Elastisearch doesn't deal with uppercase indecies
    if (!indexName.equals(indexName.toLowerCase())) {
      throw new Exception("index name must be all lower-case. Got: " + indexName);
    }

    logger.log(Level.INFO, "Checking if index: " + indexName + " exists...");
    int responseCode = executeHead(ELASTIC_URL + "/" + indexName);

    //The index exists if an only if the response code is 200.
    if (responseCode == 200) {
      logger.log(Level.INFO, "Elasticsearch index " + indexName + " exists.");
    } else {
      logger.log(Level.INFO, "Elasticsearch index " + indexName + " doesn't exist.");
    }

    return (responseCode == 200);
  }

  private HttpURLConnection createBasicConnection(String targetURL) throws IOException {
    logger.log(Level.INFO, "Indexer - Connecting to Elastic at: " + targetURL);
    URL url = new URL(targetURL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    String userpass = configuration.ELASTIC_USER + ":" + configuration.ELASTIC_PWD;
    //logger.log(Level.INFO, "Indexer - Connecting to Elastic as: " + userpass );
    String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
    //ogger.log(Level.INFO, "user and password encoded as: " + basicAuth );
    connection.setRequestProperty("Authorization", basicAuth);

    return connection;
  }

  private String executePut(String targetURL, String requestBody)
      throws Exception {
    HttpURLConnection connection = null;

    logger.log(Level.INFO, "Indexer - Executing put at: " + targetURL + " for: \n" + requestBody);
    try {
      connection = createBasicConnection(targetURL);
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Content-Type",
          "application/json");

      connection.setRequestProperty("Content-Length",
          Integer.toString(requestBody.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");

      connection.setUseCaches(false);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream(
          connection.getOutputStream());
      wr.writeBytes(requestBody);
      wr.close();

      //Get Response
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
      String line;
      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private int executeDelete(String targetURL) throws Exception {
    HttpURLConnection connection = null;
    try {
      connection = createBasicConnection(targetURL);
      connection.setDoOutput(true);
      connection.setRequestProperty(
          "Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestMethod("DELETE");
      connection.connect();
      //logger.log(Level.INFO,  "executeDelete(): Elasticsearch responded with " + connection.getResponseCode() + " : " + connection.getResponseMessage());
      return connection.getResponseCode();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private int executeHead(String targetURL) throws Exception {
    logger.log(Level.INFO, "executeHead for " + targetURL);

    HttpURLConnection connection = null;
    try {
      connection = createBasicConnection(targetURL);
      connection.setDoOutput(true);
      connection.setRequestProperty(
          "Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestMethod("HEAD");
      connection.connect();

      logger.log(Level.INFO, "executeHead(): Elasticsearch responded with " + connection.getResponseCode() + " : " + connection.getResponseMessage());

      return connection.getResponseCode();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}

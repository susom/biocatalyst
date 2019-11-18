package edu.stanford.indexer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@org.springframework.context.annotation.Configuration
public class Configuration {
  @Value("${db_user}")
  String DATALAKE_DB_USER;

  @Value("${db_pwd}")
  String DATALAKE_DB_PASSWORD;

  @Value("${db_name}")
  String DATALAKE_DB_NAME;

  @Value("${db_URL}")
  String DATALAKE_DB_HOST;

  @Value("${db_port}")
  String DATALAKE_DB_PORT;

  @Value("${elastic_user}")
  String ELASTIC_USER;

  @Value("${elastic_pwd}")
  String ELASTIC_PWD;

  @Value("${elastic_protocol}")
  String ELASTIC_PROTCOL;

  @Value("${elastic_host}")
  String ELASTIC_HOST;

  @Value("${elastic_host_docker}")
  String ELASTIC_HOST_DOCKER;

  @Value("${elastic_port}")
  String ELASTIC_PORT;

  @Value("${elastic_ssl_on}")
  String ELASTIC_SSL_ON;

  @Value("${elastic_ssl_cert}")
  String ELASTIC_SSL_CERT;

  @Value("${logstash_jdbc_driver_library}")
  String JDBC_DRIVERLIBRARY;

  @Value("${logstash_jdbc_driver_class}")
  String JDBC_DRIVER_CLASS;

  @Value("${indexer_config_file_dir}")
  String INDEXER_CONFIG_FILE_ROOT_DIR;

  @Value("${indexer_document_id_field}")
  String INDEXER_DOCUMENT_ID_FIELD;
}

package edu.stanford.integrator.services;

import edu.stanford.integrator.config.DataSourceConfig;
import java.util.List;
import java.util.Map;

public interface PreprocessorService {
  DataSourceConfig getDataSourceConfig(String remoteUser, String sourceId) throws Exception;

  String getFullDataAsJsonString(String remoteUser, DataSourceConfig sourceConfig) throws Exception;

  Map<String, Object> determineImportantTokensOrder(String rawString, String sourceId) throws Exception;

  String getOneId(DataSourceConfig sourceConfig, String jsonString, int counter) throws Exception;

  Map<String, Object> createTokenizationAndRawStringObject(String rawString) throws Exception;

  void setConfigId(String value) throws Exception;

  void setIdColumnNamePpid(String value) throws Exception;

  void setIdColumnNameMrn(String value) throws Exception;

  String transformId(DataSourceConfig sourceConfig, String rawId, String idType, Boolean applyTokenOrder, List<Integer> tokenGrowthDelimiterPositions) throws Exception;

  String applyDelimiter(String rawString, DataSourceConfig sourceConfig, List<Integer> delimiterPositionList) throws Exception;

  String[] extractImportantTokens(String[] tokens, List<Integer> importantTokensList) throws Exception;

  String[] applyImportantTokenOrder(String[] tokens, Integer[] newOrder) throws Exception;

  String[] tokenize(String rawString, Boolean stripLeadingZeros) throws Exception;
}

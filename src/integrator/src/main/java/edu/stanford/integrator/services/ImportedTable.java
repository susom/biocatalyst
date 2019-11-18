package edu.stanford.integrator.services;

import edu.stanford.integrator.config.DataSourceConfig;

public class ImportedTable {
  private String name = null;
  DataSourceConfig config = null;

  public ImportedTable(String Name, DataSourceConfig Config) {
    this.name = Name;
    this.config = Config;
  }

  public String getName() {
    return this.name;
  }

  public DataSourceConfig getConfig() {
    return this.config;
  }

  public String getSourceId() {
    return this.config.getSourceId();
  }

  public String getSqlAlias() {
    // e.g. S0 for primary, S1 for the first imported table
    return "S" + this.config.getSourceId();
  }

  public String getMatchingStrategy() {
    return this.config.getMatchingStrategy();
  }

  public String getConnectToPrimaryIdType() {
    return this.config.getConnectToPrimaryIdType();
  }

  public String getIdMrn() {
    return this.config.getIdColumnNameMrn();
  }

  public String getIdPpid() {
    return this.config.getIdColumnNamePpid();
  }

  public String getSubjectCode() {
    return this.config.getSubjectCode().toString();
  }

  public String getSubjectCodeDetails(String type) {
    return this.config.getSubjectCodeDetails(type).toString();
  }

  public String getConnectionDetails() {
    return this.config.getConnectionDetails().toString();
  }

  public String getIntegrationDetails() {
    return this.config.getIntegrationDetails().toString();
  }

  public String getConfigAsString() {
    return this.config.toString();
  }

  public String getIdColumnNamePpid() {
    return this.config.getIdColumnNamePpid();
  }

  public String getIdColumnNameMrn() {
    return this.config.getIdColumnNameMrn();
  }

  public String getVisitIdFieldFromConnections() {
    return this.config.getVisitIdFieldFromConnections();
  }

  public String getVisitIdColumn(String type) {
    return this.config.getVisitIdColumn(type);
  }

  public String getVisitIdType() {
    return this.config.getVisitIdType();
  }

  public String getJoinIdSuffix() {
    if (this.getConnectToPrimaryIdType().equalsIgnoreCase("mrn")) {
      return "_join_id_mrn";
    } else if (this.getConnectToPrimaryIdType().equalsIgnoreCase("ppid")) {
      return "_join_id_ppid";
    } else {
      return "_BAD_JOIN_ID_TYPE_REFERENCE_IN_IMPORTED_TABLE_DOT_JAVA";
    }
  }

  public int getDateMatchingRangeInDays() {
    return this.config.getDateMatchingRangeInDays();
  }

  public String getDateColumnName() {
    return this.config.getDateColumnName();
  }

  public Boolean getIsIntegratableByEvent() {
    return this.config.isIntegratableByEvent();
  }

  public Boolean getIsIntegratableByDate() {
    return this.config.isIntegratableByDate();
  }
}

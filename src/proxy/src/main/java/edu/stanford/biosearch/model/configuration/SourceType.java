package edu.stanford.biosearch.model.configuration;

import edu.stanford.biosearch.service.CsvConnectionDetailsService;
import edu.stanford.biosearch.service.EpicConnectionDetailsService;
import edu.stanford.biosearch.service.OpenspecimenConnectionDetailsService;
import edu.stanford.biosearch.service.RedcapConnectionDetailsService;

public enum SourceType {
  OPENSPECIMEN(OpenspecimenConnectionDetailsService.class),
  REDCAP(RedcapConnectionDetailsService.class),
  CSV(CsvConnectionDetailsService.class),
  EPIC(EpicConnectionDetailsService.class);

  private Class connDetailsServiceImpl;

  SourceType(Class connDetailsServiceImpl) {
    this.connDetailsServiceImpl = connDetailsServiceImpl;
  }

  public Class getConnDetailsServiceImpl() {
    return connDetailsServiceImpl;
  }

}

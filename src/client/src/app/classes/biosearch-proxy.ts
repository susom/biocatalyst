export interface IBiosearchProxy {
  responseBody: any;
  responseCode: any;
}

export interface IBiosearchProxyQuery {
  indexes: string[];
  columns: string[];
  from: number;
  filters: IBiosearchProxyFilter;
  size: number;
  sort: IBiosearchProxySort;
}

export interface IBiosearchProxyFilter {
  match: any;
  range: any;
}

export interface IBiosearchProxySort {
  mapping: any;
}

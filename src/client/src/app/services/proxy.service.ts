import { Injectable } from '@angular/core';

import { Observable, of as observableOf } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConfigurationService } from '../app.configuration';

import { IBiosearchProxy, IBiosearchProxyQuery } from '../classes/biosearch-proxy';
import { IConfiguration } from '../classes/configuration';
import { IDateRange, NumericRange } from '../classes/elastic-model';
import { Logger } from '../classes/logger';
import { IReport } from '../models/report';
import { Network } from './network';

export interface IElasticHit {
  _index: string;
  _type: string;
  _id: string;
  _score: number;
  _source: any;
}

export interface IElasticHits {
  total: number;
  max_score: number;
  hits: IElasticHit[];
}

export interface IElasticResponse {
  took: number;
  timed_out: boolean;
  _shards: any;
  hits: IElasticHits;
}

export interface IElasticDocumentModificationResponse {
  _index: string;
  _type: string;
  _id: string;
  _version: number;
  _result: string;
  _shards: any;
  created: boolean;
}

export enum ElasticFieldDataTypes {
  Keyword = 0,
  Text,
  Numeric,
  Date,
  Boolean,
  Binary,
  Range,
  Unknown
}

interface ISearchBody {
  query: string;
  aggregations?: boolean;
  highlights?: boolean;
  indexMappings?: boolean;
  indexNames?: boolean;
  from?: number;
  size?: number;
  indices?: string[];
  filters?: string[];
  sources?: string[];
}

class SearchBody implements ISearchBody {
  query: string;
  aggregations: boolean;
  highlights: boolean;
  indexMappings: boolean;
  indexNames: boolean;
  from: number;
  size: number;
  indices: string[];
  filters: string[];
  sources: string[];

  constructor(source?: ISearchBody) {
    this.query = source ? source.query : '';
    this.aggregations = source ? source.aggregations : false;
    this.highlights = source ? source.highlights : false;
    this.indexMappings = source ? source.indexMappings : false;
    this.indexNames = source ? source.indexNames : false;
    this.from = source ? source.from : 0;
    this.size = source ? source.size : 100;
    this.indices = source ? source.indices : [];
    this.filters = source ? source.filters : [];
    this.sources = source ? source.sources : [];
  }
}

@Injectable()
export class ProxyService {
  private logger = new Logger(this);
  private config: IConfiguration;

  constructor(
    private network: Network,
    private configuration: ConfigurationService) {
    this.configuration.getConfiguration().then((config) => {
      this.config = config;
    });
  }

  public async completion(fieldName: string, value: string, tableName: string): Promise<any> {
    const body = {index: tableName, field: fieldName, query: value};
    const url = this.config.biosearch_proxy_suggestion;

    return this.network.post<IBiosearchProxy>(url, body).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody;
    });
  }

  public async createConfiguration(body: any): Promise<any> {
    const url = this.config.biosearch_proxy_configuration;
    return this.network.post<IBiosearchProxy>(url, JSON.stringify(body)).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticDocumentModificationResponse;
    });
  }

  public async saveConfiguration(tableName: string, body: any): Promise<any> {
    const url = this.config.biosearch_proxy_configuration.concat('/').concat(tableName);
    return this.network.put<IBiosearchProxy>(url, JSON.stringify(body)).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticDocumentModificationResponse;
    });
  }

  public async getUserConfigurations(): Promise<IElasticHit[]> {
    const url = this.config.biosearch_proxy_configuration;
    return this.network.get<IBiosearchProxy>(url).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticResponse;
    }).then((response: IElasticResponse) => {
      const hits = response.hits.hits;

      hits.forEach(hit => {
        Object.keys(hit._source).forEach((p) => {
          const regex: RegExp = new RegExp(/suggest_.*/, 'gi');
          if (regex.test(p) || p === '@timestamp' || p === '@version') {
            delete hit._source[p];
          }
        });
      });

      return hits;
    });
  }

  public async createBookmark(bookmark: any): Promise<any> {
    const url = this.config.biosearch_proxy_bookmarks;
    return this.network.post<IBiosearchProxy>(url, JSON.stringify(bookmark)).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticResponse;
    });
  }

  public async updateBookmark(id: string, bookmark: any): Promise<any> {
    const url = `${this.config.biosearch_proxy_bookmarks}/${id}`;
    return this.network.put<IBiosearchProxy>(url, JSON.stringify(bookmark)).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticResponse;
    });
  }

  public async getBookmarks(): Promise<any> {
    const url = this.config.biosearch_proxy_bookmarks;
    return this.network.get<IBiosearchProxy>(url).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticResponse;
    }).then((response: any) => {
      return response.hits.hits;
    }).catch(error => {
      this.logger.error(error);
      return [];
    });
  }

  deleteBookmark(id: string): Observable<any> {
    const url = this.config.biosearch_proxy_bookmarks.concat('/').concat(id);
    return this.network.delete<IBiosearchProxy>(url);
  }

  getCount(id: string): Promise<any> {
    const searchBody = new SearchBody();
    searchBody.indices = [id];
    return this.globalSearch(searchBody);
  }

  public getData(index: string, columns = [], filters = null, sort = [], from = 0, size = 10, mappings = false, query: string): Promise<any> {
    const newFilters = [];

    if (filters.match) {
      Object.keys(filters.match).map((prop) => {
        const newMatch = {};
        newMatch[prop] = filters.match[prop];
        newFilters.push(newMatch);
      });
    }

    const searchBody = new SearchBody();
    searchBody.query = query;
    searchBody.indexMappings = true;
    searchBody.indexNames = true;
    searchBody.from = from;
    searchBody.size = size;
    searchBody.indices = [index];
    searchBody.filters = newFilters;

    return this.globalSearch(searchBody);
  }

  public deleteConfiguration(configId: string): Promise<any> {
    const url = `${this.config.biosearch_proxy_configuration}/${configId}`;
    return this.network.delete<IBiosearchProxy>(url).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody as IElasticResponse;
    });
  }

  getMapping(indexName: string): Observable<any> {
    const url = this.config.biosearch_proxy_integration.concat('/').concat(indexName).concat('?mapping=true').concat('&data=false');
    return this.network.post<IBiosearchProxy>(url, null).pipe(
      map((response: IBiosearchProxy) => {
        this.logger.debug('getMapping', indexName, response);
        return response.responseBody.mapping;
      }));
  }

  getPreprocessedID(config: string, sourceId: string) {
    const url = `${this.config.integrator_preprocessor_format}?config=${config}&source_id=${sourceId}&field=ppid`;
    return this.network.get(url);
  }

  evaluateID(config: string, sourceId: string) {
    const url = `${this.config.integrator_evaluate_format}?config=${config}&source_id=${sourceId}`;
    return this.network.get(url);
  }

  getStrideMetadata(irb: string): Observable<any> {
    const url = this.config.biosearch_proxy_stride_metadata.concat('/').concat(irb);
    return this.network.get<IBiosearchProxy>(url, null).pipe(
      map((response: IBiosearchProxy) => {
        this.logger.debug('getMapping', irb, response);
        return response.responseBody;
      }),
      catchError(error => observableOf(error)));
  }

  getStrideIRBs(): Observable<any> {
    const url = this.config.biosearch_proxy_stride_irbs;
    return this.network.get<IBiosearchProxy>(url, null).pipe(
      map((response: IBiosearchProxy) => {
        this.logger.debug('getStrideIRBs', response);
        return response.responseBody;
      }),
      catchError(error => observableOf(error)));
  }

  getSampleDate(config: string, sourceId: string): Observable<any> {
    const url = `${this.config.integrator_preprocessor_format}?config=${config}&source_id=${sourceId}&field=visitDate`;
    return this.network.get(url).pipe(
      map((response: any) => {
        return response.raw;
      })
    );
  }

  createItem(body: string): Observable<any> {
    const url = this.config.biosearch_proxy_localstorage_create;
    return this.network.put(url, body).pipe(
      map((response: IBiosearchProxy) => {
        this.logger.debug('getStrideIRBs', response);
        return response.responseBody;
      }),
      catchError(error => observableOf(error))
    );
  }

  public async previewItem(id: string): Promise<any> {
    const url = `${this.config.biosearch_proxy_localstorage_preview}/${id}`;
    return this.network.get(url).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody;
    });
  }

  updateItem(id: string, body: string): Observable<any> {
    const url = this.config.biosearch_proxy_localstorage_update.concat('/').concat(id);
    return this.network.post(url, body).pipe(
      map((response: IBiosearchProxy) => {
        this.logger.debug('getStrideIRBs', response);
        return response.responseBody;
      }),
      catchError(error => observableOf(error))
    );
  }

  public generateQueryFromReportBiosearchProxy(report: IReport, from = 0, size = 10000) {
    const columns = report.VisableColumns.map((c) => c.Label);
    const sort: any = {};
    const filters: any = {
      match: {},
      range: {}
    };
    let rangeFilled = false;

    report.Filters.forEach(header => {
      if (header.SortOrder) {
        let newLabel = header.Label;
        if (header.Type === ElasticFieldDataTypes.Text) {
          newLabel = header.Label.concat('.keyword');
        }
        sort[newLabel] = header.SortOrder < 0 ? 'desc' : 'asc';
      }

      if (header.Filter) {
        if (header.Type === ElasticFieldDataTypes.Numeric) {
          let numericData = header.Filter as any;
          numericData = numericData as NumericRange;
          if (numericData) {
            if (!NumericRange.isNull(numericData)) {
              const numericRange = {};
              numericRange[header.Label] = numericData;
              rangeFilled = true;
              filters.range[header.Label] = numericData;
            }
          }
        } else if (header.Type === ElasticFieldDataTypes.Date) {
          let date = header.Filter as any;
          date = date as IDateRange;
          if (date) {
            const dateRange = {};
            dateRange[header.Label] = {
              gte: date.start,
              lt: date.end
            };
            rangeFilled = true;
            filters.range[header.Label] = dateRange;
          }
        } else {
          const match: any = {};
          match[header.Label] = header.Filter;
          filters.match[header.Label] = header.Filter;
        }
      }
    });

    const Body = {
      columns,
      from,
      filters,
      size,
      sort
    };

    return Body;
  }

  public search(query: IBiosearchProxyQuery): Observable<any> {
    const url = this.config.biosearch_proxy_integration;
    return this.network.post<IBiosearchProxy>(url, JSON.stringify(query)).pipe(
      map((response: IBiosearchProxy) => {
        return response.responseBody;
      }),
      map((response: any) => {
        const hits = response.hits.hits;

        for (const hit of hits) {
          for (const p in hit._source) {
            const regex: RegExp = new RegExp(/suggest_.*/, 'gi');
            if (regex.test(p) || p === '@timestamp' || p === '@version') {
              delete hit._source[p];
            }
          }
        }

        return hits;
      }),
      catchError(error => observableOf(error)));
  }

  public globalSearch(body: SearchBody): Promise<any> {
    const url = this.config.biosearch_proxy_search;

    return this.network.post<IBiosearchProxy>(url, body).toPromise().then((response: IBiosearchProxy) => {
      return response.responseBody;
    }).then((response: any) => {
      const hits = response.hits.hits;

      hits.forEach((hit) => {
        Object.keys(hit._source).forEach((prop) => {
          const regex: RegExp = new RegExp(/suggest_.*/, 'gi');
          if (regex.test(prop) || prop === '@timestamp' || prop === '@version') {
            delete hit._source[prop];
          }
        });
      });
      return response;
    });
  }

  public convertDataType(inType: string): ElasticFieldDataTypes {
    let outType = ElasticFieldDataTypes.Unknown;
    switch (inType) {
      case 'text':
        outType = ElasticFieldDataTypes.Text;
        break;
      case 'keyword':
        outType = ElasticFieldDataTypes.Keyword;
        break;
      case 'long':
      case 'integer':
      case 'short':
      case 'byte':
      case 'double':
      case 'float':
      case 'half_float':
      case 'scaled_float':
        outType = ElasticFieldDataTypes.Numeric;
        break;
      case 'date':
        outType = ElasticFieldDataTypes.Date;
        break;
      case 'boolean':
        outType = ElasticFieldDataTypes.Boolean;
        break;
      case 'binary':
        outType = ElasticFieldDataTypes.Binary;
        break;
      case 'integer_range':
      case 'float_range':
      case 'long_range':
      case 'double_range':
      case 'date_range':
        outType = ElasticFieldDataTypes.Range;
        break;
    }
    return outType;
  }
}

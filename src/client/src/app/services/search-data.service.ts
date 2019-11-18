import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

import { Logger } from '../classes/logger';
import { Util } from '../classes/system/util';
import { IReport, Report } from '../models/report';
import { IElasticResponse, ProxyService } from './proxy.service';
import { ReportNavigationService } from './report-navigation.service';
import { ReportStoreService } from './report-store.service';
import { SessionManager } from './session-manager.service';

export interface ITypeInfo {
  type: any;
  fields: any;
}

export interface IColumn {
  group?: number;
  name?: string;
  displayLabel?: string;
  hidden?: boolean;
  order?: number;
  typeInfo?: ITypeInfo;
}

export class Column implements IColumn {
  public group: number;
  public name: string;
  public displayLabel: string;
  public hidden: boolean;
  public order: number;
  public typeInfo: ITypeInfo;

  public constructor(source?: IColumn) {
    this.group = source ? source.group : -1;
    this.name = source ? source.name : '';
    this.hidden = source ? source.hidden : false;
    this.displayLabel = source ? source.displayLabel : name;
    this.adjustDisplayLabel();
    this.adjustGroupNumber();
  }

  private adjustDisplayLabel() {
    const regex = new RegExp(/^j\d+\_s\d\d_?/);
    const execArray = regex.exec(this.displayLabel);
    if (execArray) {
      this.displayLabel = this.displayLabel.replace(execArray[0], '');
    }
    this.displayLabel = SessionManager.FormatString(this.displayLabel);
  }

  private adjustGroupNumber() {
    const regExp = new RegExp(/s\d\d?_/);
    const execArray = regExp.exec(this.name);

    if (execArray) {
      const sourceNumber = Number(execArray[0].charAt(1)) + 1;
      this.group = sourceNumber;
    }
  }
}

@Injectable()
export class SearchDataService {
  private logger = new Logger(this);

  public columns: BehaviorSubject<any[]>;
  public data: BehaviorSubject<any[]>;

  public start = 0;
  public initialRequestSize = 6500;
  public requestSize = 6500;
  public displaySize = 6500;
  public total = 0;

  private report: IReport;

  constructor(
    private reportNavigation: ReportNavigationService,
    private reportStore: ReportStoreService,
    private proxyService: ProxyService
  ) {
    this.columns = new BehaviorSubject<any[]>([]);
    this.data = new BehaviorSubject<any[]>([]);

    this.reportNavigation.selectedReportChanged.subscribe(report => {
      if (report) {
        this.report = report;
        this.refreshData();
      }
    });
  }

  public refreshData(): Promise<any[]> {
    if (this.report.IntegrationID && this.report.IntegrationID.length > 0) {
      const body = this.proxyService.generateQueryFromReportBiosearchProxy(this.report, this.start, this.initialRequestSize);
      return this.fetchData(this.report.IntegrationID, body, true, false, this.report.Query);
    }
    return Promise.resolve([]);
  }

  private processMapping(Mapping: any): void {
    const columns = [];
    Object.keys(Mapping).forEach(p => {
      // Removing timestamp and version, should be removed in the backend
      const regex: RegExp = new RegExp(/suggest_.*/, 'gi');
      if (regex.test(p) || p === '@timestamp' || p === '@version') {
        delete Mapping[p];
      } else {
        const entry = Mapping[p];
        if (entry.type) {
          entry.type = this.proxyService.convertDataType(entry.type);
        }

        let hidden = true;
        let order = -1;
        this.report.Filters.every(header => {
          if (header.Label === p) {
            hidden = header.Hidden;
            order = header.Order;
            return false;
          }
          return true;
        });
        columns.push(new Column({name: p, hidden, order, typeInfo: Mapping[p]}));
      }
    });

    columns.sort((A: any, B: any) => Util.strcmp((A as Column).displayLabel, (B as Column).displayLabel));

    this.columns.next(columns);
  }

  private processIntegration(Integration: IElasticResponse, appendData: boolean): void {
    let data = [];
    const hits = Integration.hits.hits;
    if (!appendData) {
      data = [];
    } else {
      data = this.data.getValue();
    }

    hits.forEach((hit) => {
      Object.keys(hit._source).forEach((p) => {
        const regex: RegExp = new RegExp(/suggest_.*/, 'gi');
        if (regex.test(p) || p === '@timestamp' || p === '@version') {
          delete hit._source[p];
        }
        hit._source['@biocatalyst-id'] = hit._index.concat(hit._id);
      });
      data.push(hit._source);
    });

    this.data.next(data);
  }

  private setTotal(total: number): void {
    this.total = total;
  }

  public fetchData(id: string, filter: any, mappings = true, appendData = false, query): Promise<any> {
    if (!appendData) {
      this.start = 0;
    }

    return this.proxyService.getData(
      id, filter.columns, filter.filters, filter.sort, filter.from, filter.size, mappings, query
    ).then(response => {
      if (response.indexmappings && response.indexmappings[id]) {
        this.processMapping(response.indexmappings[id]);
      }

      if (response.hits) {
        this.processIntegration(response, appendData);
        this.setTotal(response.hits.total);
      }

      return this.fetchMore();
    });
  }

  public fetchMore(): Promise<any> {
    if (this.data.getValue().length < this.total) {
      this.start = this.data.getValue().length;
      this.displaySize += this.requestSize;
      const body = this.proxyService.generateQueryFromReportBiosearchProxy(this.report, this.start, this.requestSize);
      return this.fetchData(this.report.IntegrationID, body, false, true, this.report.Query);
    }
    return Promise.resolve(null);
  }
}

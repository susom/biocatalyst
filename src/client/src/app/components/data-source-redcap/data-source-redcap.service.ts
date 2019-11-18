import { EventEmitter, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, filter, switchMap, tap } from 'rxjs/operators';
import { ConfigurationService } from '../../app.configuration';
import { IConfiguration } from '../../classes/configuration';
import { Logger } from '../../classes/logger';
import { Network } from '../../services/network';
import { ProxyService } from '../../services/proxy.service';

export enum URLREQUESTSTATE {
  'Default' = 0,
  'Requesting' = 1,
  'Returned' = 2
}

// Interfaces with Redcap. Gets the Projects, Gets the Reports, Gets the Columns
@Injectable()
export class DataSourceRedcapService {
  private config: IConfiguration;

  constructor(
    private network: Network,
    private biosearchProxyService: ProxyService,
    private configurationService: ConfigurationService) {
    this.configurationService.getConfiguration().then((config) => {
      this.config = config;
    });
  }

  public get Projects$(): Observable<any[]> {
    return this.projectList;
  }

  public get Reports$(): Observable<any[]> {
    return this.reportList;
  }

  public get Columns$(): Observable<any[]> {
    return this.columnList;
  }

  private logger = new Logger(this);

  private projectList: BehaviorSubject<any[]> = new BehaviorSubject<any[]>([]);
  private reportList: BehaviorSubject<any[]> = new BehaviorSubject<any[]>([]);
  private columnList: BehaviorSubject<any[]> = new BehaviorSubject<any[]>([]);

  private triggerGetProjects = new EventEmitter();
  private projectsStatus = new BehaviorSubject<URLREQUESTSTATE>(URLREQUESTSTATE.Default);
  private projectChain = this.triggerGetProjects
    .pipe(
      switchMap(() => {
        const url = this.config.biosearch_proxy_redcap_projects;
        this.projectsStatus.next(URLREQUESTSTATE.Requesting);
        return this.network.get<any[]>(url).pipe(
          tap((result) => {
            this.projectList.next(result as any[]);
            this.projectsStatus.next(URLREQUESTSTATE.Returned);
          }));
      })
    )
    .subscribe();

  private triggerGetReports = new EventEmitter();
  private reportsStatus = new BehaviorSubject<URLREQUESTSTATE>(URLREQUESTSTATE.Default);
  private reportsChain = this.reportsChainSubsctiption();

  private triggerGetColumns = new EventEmitter();
  private columnsStatus = new BehaviorSubject<URLREQUESTSTATE>(URLREQUESTSTATE.Default);
  private columnsChain = this.columnsChainSubsctiption();

  private reportsChainSubsctiption() {
    return this.triggerGetReports
      .pipe(
        filter(report => {
          return report != null;
        }),
        switchMap(report => {
          this.logger.debug('triggerGetReports');
          this.reportsStatus.next(URLREQUESTSTATE.Requesting);
          const reportsURL = this.config.biosearch_proxy_redcap_reports.concat('/' + report);
          return this.network.get<any>(reportsURL);
        }),
        catchError(error => {
          // Need to reset as this subscription will be lost if there is an error.
          this.reportsChain = this.reportsChainSubsctiption();
          return of({
            responseBody: {
              reports: []
            }
          });
        }),
        tap(response => {
          this.reportList.next(response.responseBody.reports as any[]);
          this.logger.debug('Redcap Reports Returned: ', this.reportList);
          this.reportsStatus.next(URLREQUESTSTATE.Returned);
        })
      )
      .subscribe();
  }

  public columnsChainSubsctiption() {
    return this.triggerGetColumns.pipe(
      filter(request => {
        if (request == null) {
          return false;
        }
        if (request[0] == null) {
          return false;
        }
        if (request[1] == null) {
          return false;
        }
        return true;
      }),
      switchMap(request => {
        this.logger.debug('triggerGetColumns', this.columnList);
        this.columnsStatus.next(URLREQUESTSTATE.Requesting);

        if (!request) {
          return of({
            responseBody: []
          });
        }

        const reportsURL = this.config.biosearch_proxy_redcap_columns
          .concat('/').concat(request[0])
          .concat('/').concat(request[1]);
        return this.network.get<any>(reportsURL);
      }),
      catchError(error => {
        this.columnsChain = this.columnsChainSubsctiption();
        return of({
          responseBody: []
        });
      }),
      tap(response => {
        this.columnList.next(response.responseBody as any[]);
        this.logger.debug('Redcap Columns Returned', this.columnList);
        this.columnsStatus.next(URLREQUESTSTATE.Returned);
      })
    )
      .subscribe();
  }

  public TriggerGetProjects() {
    this.triggerGetProjects.emit();
  }

  public TriggerGetReports(ReportID: string) {
    this.triggerGetReports.emit(ReportID);
  }

  public TriggerGetColumns(One: string, Two: string) {
    this.triggerGetColumns.emit([One, Two]);
  }

  public FindReport(ReportID: string) {
    const list = this.reportList.getValue();
    const found = list.find(report => {
      return report.report_id === ReportID;
    });
    return found;
  }

  public FindColumn(ColumnName: string) {
    const list = this.columnList.getValue();
    const found = list.find(column => {
      return column.field_name === ColumnName;
    });
    return found;
  }
}

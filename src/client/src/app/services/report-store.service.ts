import { EventEmitter, Injectable } from '@angular/core';
import { now } from 'moment';
import { Logger } from '../classes/logger';
import { IReport } from '../models/report';
import { IElasticHit, ProxyService } from './proxy.service';

@Injectable()
export class ReportStoreService {
  private logger: Logger = new Logger(this);
  private reports: IReport[];
  public reportsUpdated: EventEmitter<IReport[]>;

  private delayUntilTime: number;

  constructor(private elastic: ProxyService) {
    this.reportsUpdated = new EventEmitter<IReport[]>();
    this.delayUntilTime = 0;
  }

  public async getReports(): Promise<void> {
    if (now() > this.delayUntilTime) {
      this.delayUntilTime = now() + 5000;
      this.reports = await this.fetch();
    }
    this.reportsUpdated.emit(this.reports);
  }

  private async fetch(): Promise<IReport[]> {
    this.logger.debug('Fetching Reports from Proxy Service');
    return this.elastic.getBookmarks().then(async (reports: IElasticHit[]) => {
      return reports.map(report => {
        this.saveReport(report._source);
        return report._source as IReport;
      });
    });
  }

  saveReport(report: IReport) {
    if (this.reports.find(r => report._ids === r._ids)) {
      this.updateReport(report);
    } else {
      this.addReport(report);
    }
  }

  addReport(report: IReport) {
    this.reports.push(report);
    this.reportsUpdated.emit(this.reports);
  }

  updateReport(report: IReport) {
    // FIXME: This mutates the report inline and should replace it instead. (PJS)
    Object.assign(this.reports.find(r => report._ids === r._ids), report);
  }

  deleteReport(report: IReport): Promise<boolean> {
    this.reports = this.reports.filter(r => report._ids !== r._ids);
    return this.elastic.deleteBookmark(report._ids).toPromise();
  }

  set(reports: IReport[]) {
    this.reports = reports;
    this.reportsUpdated.emit(this.reports);
  }

  public async saveAllToCloud(): Promise<any> {
    const saves = this.reports.map(report => {
      return this.saveToCloud(report);
    });

    return Promise.all(saves);
  }

  public async saveToCloud(report: IReport): Promise<any> {
    return this.createCloudSave(report);
  }

  public async createCloudSave(report: IReport): Promise<any> {
    return report._ids ?
      await this.elastic.updateBookmark(report._ids, report) :
      this.registerReportToCloud(report);
  }

  public async registerReportToCloud(report: IReport): Promise<any> {
    return this.elastic.createBookmark(report).then((response: any) => {
      report._ids = response._id;
      return this.saveToCloud(report);
    });
  }
}

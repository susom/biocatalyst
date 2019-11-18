import { EventEmitter, Injectable } from '@angular/core';
import { Logger } from '../classes/logger';
import { IReport, Report } from '../models/report';
import { ReportStoreService } from './report-store.service';

@Injectable()
export class ReportNavigationService {
  private logger: Logger = new Logger(this);

  private selectedReport: IReport;
  private selectedIndex: number;
  private reports: IReport[] = [];
  public selectedReportChanged: EventEmitter<IReport>;

  constructor(private reportStore: ReportStoreService) {
    this.selectedReport = null;
    this.selectedIndex = -1;
    this.reports = [];
    this.selectedReportChanged = new EventEmitter<IReport>();

    this.reportStore.reportsUpdated.subscribe(reports => {
      this.logger.debug('Reports have been updated');
      if (reports && reports.length > 0) {
        this.reports = reports;
        this.selectedReport = reports[0];
        this.selectedIndex = 0;
        this.selectedReportChanged.emit(this.selectedReport);
      }
    });
  }

  public emptySelect(): void {
    this.logger.debug('Unselected Report')
    this.selectedReport = null;
    this.selectedIndex = -1;
    this.selectedReportChanged.emit(this.selectedReport);
  }

  public selectReport(report: IReport): void {
    const found = this.reports.find((r: IReport) => r._ids === report._ids);
    if (found) {
      this.logger.debug('Report has been integration');
      this.selectedReport = found;
      this.selectedReportChanged.emit(this.selectedReport);
    } else {
      this.logger.debug('Report not found for selection');
      this.emptySelect();
    }
  }
}

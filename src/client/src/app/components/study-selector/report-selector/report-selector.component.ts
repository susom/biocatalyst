import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Logger } from '../../../classes/logger';
import { IIntegration, Integration } from '../../../models/integration';
import { IReport } from '../../../models/report';
import { ReportStoreService } from '../../../services/report-store.service';

@Component({
  selector: 'app-report-selector',
  templateUrl: './report-selector.component.html',
  styleUrls: ['./report-selector.component.scss']
})
export class ReportSelectorComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  @Input() reports: IReport[];
  @Input() integration: IIntegration;
  @Input() report: IReport;
  @Output() reportChange: EventEmitter<IReport>;

  reportOptions: IReport[];

  constructor(public reportStore: ReportStoreService) {
    this.reportChange = new EventEmitter<IReport>();
  }

  ngOnInit() {
    this.updateReportOptions();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.reports || changes.selectedIntegration) {
      this.updateReportOptions();
    }
  }

  private updateReportOptions() {
    if (this.integration) {
      this.reportOptions = this.reports.filter(r => r.IntegrationID === this.integration._ids);
    }
  }

  selectReport(event) {
    if (this.report !== event.selectedItem) {
      this.report = event.selectedItem;
      this.reportChange.emit(this.report);
    }
  }

  deleteReport(report) {
    this.reportStore.deleteReport(report);
  }
}

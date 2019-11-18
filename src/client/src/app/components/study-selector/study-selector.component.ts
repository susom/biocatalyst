import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Logger } from '../../classes/logger';
import { IIntegration } from '../../models/integration';
import { IReport } from '../../models/report';

@Component({
  selector: 'app-study-selector',
  templateUrl: './study-selector.component.html',
  styleUrls: ['./study-selector.component.scss']
})
export class StudySelectorComponent {
  private logger = new Logger(this);

  @Input() integrations: IIntegration[];
  @Input() integration: IIntegration;
  @Input() reports: IReport[];
  @Input() report: IReport;
  @Output() selectedChange: EventEmitter<{ integration, report }>;

  constructor() {
    this.selectedChange = new EventEmitter<{ integration: IIntegration, report: IReport }>();
  }

  public onIntegrationChange(event): void {
    if (event.selectedItem && event.selectedItem !== this.integration) {
      this.selectedChange.emit({integration: event.selectedItem, report: this.report});
    }
  }

  public onReportChange(event): void {
    this.selectedChange.emit({integration: this.integration, report: event});
  }
}

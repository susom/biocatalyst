import { Component, EventEmitter, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material';
import { Message } from 'primeng/components/common/api';
import { Observable, Subscription, timer } from 'rxjs';
import { Logger } from '../../classes/logger';
import { ImportStatus } from '../../models/import-status';
import { IIntegration, Integration } from '../../models/integration';
import { ImporterService } from '../../services/importer.service';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { IntegrationStoreService } from '../../services/integration-store.service';
import { ErrorModalComponent } from '../error-modal/error-modal.component';

@Component({
  selector: 'app-integration-configuration',
  templateUrl: './integration-configuration.component.html',
  styleUrls: ['./integration-configuration.component.scss']
})
export class IntegrationConfigurationComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  public integration: IIntegration;
  public status: Message[] = [];
  public dataConnected: boolean;

  private timer: Observable<number>;
  private timerSubscription: Subscription;
  private timerActive: boolean;
  private previousTitle: string;

  constructor(private integrationNavigation: IntegrationNavigationService,
              private integrationStore: IntegrationStoreService,
              private importer: ImporterService,
              private dialog: MatDialog) {
    this.integration = new Integration();
    this.timer = timer(0, 5000);
    this.timerSubscription = null;
    this.timerActive = false;
    this.dataConnected = false;
    this.previousTitle = '';
  }

  public ngOnInit(): void {
    this.updateIntegration(this.integrationNavigation.getSelectedIntegration());

    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.status = [];
      this.updateIntegration(integration);
    });
  }

  public ngOnChanges(changes: SimpleChanges): void {
  }

  public sync(): void {
    this.importer.createView(this.integration._ids).subscribe(results => {
      this.startTimer();
      this.logger.debug('Creating View', results);
    });
  }

  public async saveConfiguration(): Promise<IIntegration> {
    await this.integrationStore.saveIntegration(this.integration);
    return this.integrationStore.saveToCloud(this.integration);
  }

  private updateIntegration(integration) {
    if (!!integration) {
      this.stopTimer();
      this.integration = integration;
      this.startTimer();
    }
  }

  private startTimer(): void {
    if (!this.timerSubscription) {
      this.timerActive = true;
      this.timerSubscription = this.timer.subscribe(() => {
        this.logger.debug(`Checking Status: ${this.integration._ids}`);
        this.importer.checkViewStatus(this.integration._ids).subscribe((response) => {
          switch (response.state) {
            case 'INACTIVE':
              this.dataConnected = true;
              break;
            case 'ACTIVE':
            default:
              this.dataConnected = false;
              break;
          }
          this.status = ImportStatus.fromObjectToMessages(response, (this.timerActive) ? 'info' : null);
          this.stopTimer();
        });
      });
    }
  }

  private stopTimer(): void {
    if (this.timerSubscription) {
      this.timerActive = false;
      this.timerSubscription.unsubscribe();
      this.timerSubscription = null;
    }
  }

  private launchSameNameErrorModal(text: string) {
    const error = `"${text}" already exists! Please use a different name.`;
    const config = new MatDialogConfig();
    config.height = '200px';
    config.width = '300px';
    config.data = {
      message: error
    };
    this.dialog.open(ErrorModalComponent, config);
  }
}

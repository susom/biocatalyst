import { Component } from '@angular/core';
import { RouterTransition } from './animations/router-transition';
import { ConfigurationService } from './app.configuration';
import { Logger } from './classes/logger';
import { CollectionProtocolsService } from './services/collection-protocols.service';
import { IntegrationStoreService } from './services/integration-store.service';
import { ReportNavigationService } from './services/report-navigation.service';
import { ReportStoreService } from './services/report-store.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [RouterTransition]
})
export class AppComponent {
  logger: Logger = new Logger(this);

  constructor(
    private collectionProtocols: CollectionProtocolsService,
    private configuration: ConfigurationService,
    private integrationStore: IntegrationStoreService,
    private reportNavigation: ReportNavigationService,
    private reportStore: ReportStoreService
  ) {
    this.initialize().then(() => {
      this.logger.debug('onLoadComplete');
      this.reportNavigation.selectReport(null);
    }).catch((error) => {
      this.logger.error(`Error initializing application: ${error.message}`);
    });
  }

  private async initialize() {
    try {
      await this.configuration.getConfiguration();

      await Promise.all([
        this.reportStore.getReports(),
        this.integrationStore.getIntegrations(),
        this.collectionProtocols.getProtocols()
      ]);
    } catch (error) {
      this.logger.error(`Error fetching initial data: ${error.message}`);
    }
  }
}

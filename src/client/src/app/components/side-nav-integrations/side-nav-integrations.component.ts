import { Component, OnDestroy, OnInit } from '@angular/core';
import { Logger } from '../../classes/logger';
import { IIntegration } from '../../models/integration';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { IntegrationStoreService } from '../../services/integration-store.service';

@Component({
  selector: 'app-side-nav-integrations',
  templateUrl: './side-nav-integrations.component.html',
  styleUrls: ['./side-nav-integrations.component.scss']
})
export class SideNavIntegrationsComponent implements OnInit, OnDestroy {
  private logger = new Logger(this);

  public selectedIndex;
  public integrations: IIntegration[] = [];

  constructor(
    private integrationStore: IntegrationStoreService,
    private integrationNavigation: IntegrationNavigationService
  ) {
    this.selectedIndex = 0;
  }

  ngOnInit() {
    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.logger.debug(`Selected Integration Updated`);
      this.selectedIndex = index > 0 ? index : 0;
    });

    this.integrationStore.integrationsUpdated.subscribe((integrations) => {
      this.logger.debug(`Integrations Updated: ${integrations.length}`);
      this.integrations = integrations;
    });
    this.integrationStore.getIntegrations();
  }

  public integrationClick(integration: IIntegration) {
    this.integrationNavigation.selectIntegration(integration);
  }

  public addItem(): void {
    this.integrationStore.createNewIntegration().then((integration) => {
      this.integrationNavigation.selectIntegration(integration);
    });
  }

  public delete(index: number): void {
    this.integrationStore.fullDelete(this.integrations[index]).then(() => {
      this.integrationNavigation.selectIntegration(this.integrations[0]);
    }).catch((error) => {
      this.logger.error(error);
    });
  }

  public ngOnDestroy(): void {
    this.selectedIndex = 0;
    this.integrationStore.clearUnsaved();
  }
}

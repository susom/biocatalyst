import { EventEmitter, Injectable } from '@angular/core';
import { Logger } from '../classes/logger';
import { IIntegration } from '../models/integration';
import { IntegrationStoreService } from './integration-store.service';

@Injectable()
export class IntegrationNavigationService {
  private logger = new Logger(this);

  private integrations: IIntegration[] = [];
  private selectedIntegration: IIntegration;
  private selectedIndex: number;
  public selectedIntegrationChanged: EventEmitter<[IIntegration, number]>;

  constructor(private integrationStore: IntegrationStoreService) {
    this.selectedIntegrationChanged = new EventEmitter<[IIntegration, number]>();

    this.integrationStore.integrationsUpdated.subscribe(integrations => {
      this.integrations = integrations;
    });
  }

  public selectIntegration(integration: IIntegration): void {
    this.integrations.forEach((i: IIntegration, index: number) => {
      if (i._ids === integration._ids) {
        this.selectIntegrationWithIndex(i, index);
      }
    });
  }

  public selectIntegrationById(id: string) {
    this.integrations.forEach((i: IIntegration, index: number) => {
      if (i._ids === id) {
        this.selectIntegrationWithIndex(i, index);
      }
    });
  }

  public getSelectedIntegration(): IIntegration {
    return this.selectedIntegration;
  }

  private selectIntegrationWithIndex(integration: IIntegration, index: number): void {
    this.logger.debug(`Integration Selected: ${integration._ids}`);
    this.selectedIntegration = integration;
    this.selectedIndex = index;
    this.selectedIntegrationChanged.emit([this.selectedIntegration, this.selectedIndex]);
  }
}

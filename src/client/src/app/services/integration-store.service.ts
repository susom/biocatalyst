import { EventEmitter, Injectable } from '@angular/core';
import { now } from 'moment';

import { Logger } from '../classes/logger';
import { DataSource, SourceType } from '../models/data-source';
import { IIntegration, Integration } from '../models/integration';
import { IElasticDocumentModificationResponse, IElasticHit, ProxyService } from './proxy.service';

@Injectable()
export class IntegrationStoreService {
  private logger: Logger = new Logger(this);

  private integrations: IIntegration[];
  public integrationsUpdated: EventEmitter<IIntegration[]>;

  private delayUntilTime: number;

  constructor(private proxyService: ProxyService) {
    this.integrations = [];
    this.integrationsUpdated = new EventEmitter<IIntegration[]>();

    this.delayUntilTime = 0;
  }

  public async getIntegrations(): Promise<void> {
    if (now() > this.delayUntilTime) {
      this.delayUntilTime = now() + 5000;
      this.integrations = await this.fetch();
    }
    this.integrationsUpdated.emit(this.integrations);
  }

  private async fetch(): Promise<IIntegration[]> {
    return this.proxyService.getUserConfigurations().then((hits: IElasticHit[]) => {
      const integrations: IIntegration[] = [];

      if (hits) {
        hits.forEach((hit: IElasticHit) => {
          const integration = hit._source as IIntegration;
          integrations.push(new Integration(integration));
        });
      }

      if (integrations.length <= 0) {
        const newIntegration = this.createDefaultIntegration();
        newIntegration.name = this.generateName(newIntegration.name);
        return this.saveToCloud(newIntegration).then(() => {
          integrations.push(newIntegration);
          return integrations;
        });
      }

      return integrations;
    });
  }

  public findIntegrationByName(integrationName: string): IIntegration {
    return this.integrations.find(integration => integration.name === integrationName);
  }

  public findIntegrationById(id: string): IIntegration {
    return this.integrations.find(integration => integration._ids === id);
  }

  public createDefaultIntegration(): IIntegration {
    const newIntegration = new Integration({
      name: 'My First Data-set',
      _ids: null,
      dataSources: [new DataSource()]
    });

    // Neccessary for how connection_details.credentials create's itself when instantiated via the New Integration
    // Otherwise the credentials will be overwritten.
    const source = newIntegration.dataSources[0];
    source.name = 'Specimen Inventory Data';
    source.source_id = '0'; // Source Id's are zero indexed
    source.primary = true;
    source.deletable = false;
    source.valid = false;
    source.type = SourceType.OPENSPECIMEN;
    source.connection_details.URL = null;
    source.connection_details.credentials = null;

    return newIntegration;
  }

  public generateName(candidate: string): string {
    let largestAffix = -1;
    this.integrations.forEach((i: IIntegration) => {
      const regex: RegExp = new RegExp(/^(my\ new\ integration\ {0,1}(\(\d*\))?)$/, 'gi');

      const matches = regex.exec(i.name);
      if (matches) {
        if (matches[2]) {
          const affix = matches[2];
          const strippedParensAffix = affix.trim().slice(1, affix.length - 1);
          const affixNumber = Number(strippedParensAffix);
          largestAffix = (affixNumber > largestAffix) ? affixNumber : largestAffix;
        } else {
          largestAffix = 0;
        }
      }
    });

    return (largestAffix >= 0) ? `${candidate} (${largestAffix + 1})` : candidate;
  }

  public addIntegration(integration: IIntegration): void {
    integration.name = this.generateName(integration.name);
    this.integrations.push(integration);
  }

  public updateIntegration(integration: IIntegration): void {
    // FIXME: This mutates the report inline and should replace it instead. (PJS)
    Object.assign(this.integrations.find((i) => integration._ids === i._ids), integration);
  }

  public saveIntegration(integration: IIntegration): void {
    if (this.integrations.find(i => integration._ids === i._ids)) {
      this.updateIntegration(integration);
    } else {
      this.addIntegration(integration);
    }
    this.integrationsUpdated.emit(this.integrations);
  }

  public async fullDelete(integration: IIntegration): Promise<void> {
    try {
      await this.deleteFromCloud(integration);
      await this.deleteLocally(integration);
      this.integrationsUpdated.emit(this.integrations);
    } catch (error) {
      this.logger.error(`Error deleting integration: ${integration._ids}`);
    }
  }

  private async deleteLocally(integration: IIntegration): Promise<void> {
    this.integrations = this.integrations.filter((i) => i._ids !== integration._ids);
  }

  private async deleteFromCloud(integration: IIntegration): Promise<void> {
    return this.proxyService.deleteConfiguration(integration._ids);
  }

  public async saveAllToCloud(): Promise<any> {
    const posts = this.integrations.map(integration => {
      return this.saveToCloud(integration);
    });

    return Promise.all(posts);
  }

  public saveToCloud(integration: IIntegration): Promise<IIntegration> {
    return this.createCloudSave(integration);
  }

  public async createCloudSave(integration: IIntegration): Promise<IIntegration> {
    if (integration._ids) {
      this.logger.debug(`Configuration updated: ${integration._ids}`);
      return this.proxyService.saveConfiguration(integration._ids, integration);
    } else {
      return this.registerIntegrationToCloud(integration);
    }
  }

  public async registerIntegrationToCloud(integration: IIntegration): Promise<IIntegration> {
    this.logger.debug(`Creating configuration`);
    return this.proxyService.createConfiguration(integration).then((response: IElasticDocumentModificationResponse) => {
      this.logger.debug(`Configuration Created: ${response._id}`);
      integration._ids = response._id;
      return integration;
    });
  }

  public clearUnsaved(): void {
    const integration = this.findIntegrationById(null);
    if (integration) {
      this.deleteLocally(integration);
    }
  }

  public async createNewIntegration(): Promise<IIntegration> {
    this.logger.debug(`Creating a new integration configuration`);
    let integration = this.findIntegrationById(null) as Integration;
    if (!integration) {
      integration = new Integration();
      integration.name = this.generateName(integration.name);

      const newSource = new DataSource();
      newSource.deletable = false;
      newSource.primary = true;
      newSource.source_id = '0';
      newSource.name = integration.avoidNameCollision('Specimen Inventory Data');

      integration.dataSources.push(newSource);
      integration.valid = false;
      await this.saveIntegration(integration);
    }
    return this.registerIntegrationToCloud(integration);
  }
}

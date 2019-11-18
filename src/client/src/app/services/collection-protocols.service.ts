import { EventEmitter, Injectable } from '@angular/core';
import { ConfigurationService } from '../app.configuration';
import { IConfiguration } from '../classes/configuration';
import { Logger } from '../classes/logger';
import { Network } from './network';

export interface ICollectionProtocolResponse {
  responseBody: string;
  responseCode: string;
}

export interface ICollectionProtocol {
  id: any;
  shortTitle: any;
  title: any;
  code: any;
  principalInvestigator: any;
  startDate: any;
  endDate: any;
  participantCount: any;
  specimenCount: any;
  ppidFmt: any;
  manualPpidEnabled: any;
}

@Injectable()
export class CollectionProtocolsService {
  private logger = new Logger(this);

  private config: IConfiguration;
  private cachedRequest: Promise<ICollectionProtocol[]>;
  private protocols: ICollectionProtocol[];
  public protocolsUpdated = new EventEmitter<ICollectionProtocol[]>();

  constructor(
    private network: Network,
    private configuration: ConfigurationService) {
    this.configuration.getConfiguration().then((config) => {
      this.config = config;
    });
  }

  public async getProtocols(): Promise<ICollectionProtocol[]> {
    if (!this.cachedRequest) {
      this.cachedRequest = this.fetch();
    }
    this.protocols = await this.cachedRequest;
    this.protocolsUpdated.emit(this.protocols);
    return this.protocols;
  }

  private async fetch(): Promise<any> {
    return this.network.get<ICollectionProtocolResponse>(this.config.biosearch_proxy_collection_protocols).toPromise().then(response => {
      return JSON.parse(response.responseBody);
    }).catch(error => {
      this.logger.error(error);
      return [];
    }).then((result: any) => {
      this.protocols = result;
      this.protocolsUpdated.emit(this.protocols);
    });
  }
}

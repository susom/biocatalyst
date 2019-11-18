import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfigurationService } from '../app.configuration';
import { IConfiguration } from '../classes/configuration';
import { Network } from './network';

@Injectable()
export class ImporterService {
  private config: IConfiguration;

  constructor(
    private network: Network,
    private configurationService: ConfigurationService
  ) {
    this.configurationService.getConfiguration().then((config) => {
      this.config = config;
    });
  }

  public createView(studyId: string): Observable<any> {
    const url = `${this.config.integrator_integrate_format}?config=${studyId}`;
    return this.network.post(url, '');
  }

  public checkViewStatus(studyId: string): Observable<any> {
    const url = `${this.config.integrator_status_format}?config=${studyId}`;
    return this.network.get(url);
  }
}

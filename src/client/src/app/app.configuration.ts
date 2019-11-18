import { Injectable } from '@angular/core';
import { IConfiguration } from './classes/configuration';
import { ServerEnvironmentService } from './services/server-environment.service';

/**
 * Configuration Service for fetching and caching the system configuration for use throughout
 * the client application.
 */
@Injectable()
export class ConfigurationService {
  private configuration: IConfiguration;

  constructor(private serverEnvironment: ServerEnvironmentService) {
  }

  /**
   * Fetches and caches configuration settings for the application from the hosting service.
   */
  async getConfiguration(): Promise<IConfiguration> {
    if (!this.configuration) {
      this.configuration = await this.serverEnvironment.fetchConfiguration();
    }
    return this.configuration;
  }
}

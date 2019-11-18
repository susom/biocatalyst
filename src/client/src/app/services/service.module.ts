import { NgModule } from '@angular/core';

import { CookieService } from 'ngx-cookie-service';

import { AuthenticatedGuard } from './authenticated.guard';
import { AuthenticationService } from './authentication.service';
import { ProxyService } from './proxy.service';
import { CollectionProtocolsService } from './collection-protocols.service';
import { ImporterService } from './importer.service';
import { IntegrationNavigationService } from './integration-navigation.service';
import { IntegrationStoreService } from './integration-store.service';
import { ModalDialogService } from './modal-dialog.service';
import { Network } from './network';
import { ReportNavigationService } from './report-navigation.service';
import { ReportStoreService } from './report-store.service';
import { SearchDataService } from './search-data.service';
import { ServerEnvironmentService } from './server-environment.service';

@NgModule({
  providers: [
    AuthenticatedGuard,
    AuthenticationService,
    ProxyService,
    CollectionProtocolsService,
    CookieService,
    ImporterService,
    IntegrationNavigationService,
    IntegrationStoreService,
    ModalDialogService,
    Network,
    ReportNavigationService,
    ReportStoreService,
    SearchDataService,
    ServerEnvironmentService
  ],
  declarations: []
})
export class ServiceModule {
}

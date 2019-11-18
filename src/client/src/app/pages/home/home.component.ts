import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Logger } from '../../classes/logger';
import { IIntegration } from '../../models/integration';
import { ImporterService } from '../../services/importer.service';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { IntegrationStoreService } from '../../services/integration-store.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  private static readonly input: string = 'input';
  private logger = new Logger(this);

  public integrations: IIntegration[] = [];
  public dataLoading = false;
  public myForm: FormGroup = null;
  public selectedIntegration: IIntegration;
  public id: any;

  private selectedIndex;

  constructor(
    private route: ActivatedRoute,
    private integrationStore: IntegrationStoreService,
    private integrationNavigation: IntegrationNavigationService,
    private importer: ImporterService,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.dataLoading = true;

    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.selectedIndex = index;
      this.selectedIntegration = integration;
    });

    this.integrationStore.integrationsUpdated.subscribe((integrations) => {
      this.logger.debug(`Integrations Updated: ${integrations.length}`);
      this.dataLoading = false;
      this.integrations = integrations;
    });
    this.integrationStore.getIntegrations();

    const formControlObject = {};
    formControlObject[HomeComponent.input] = new FormControl('');
    this.myForm = new FormGroup(formControlObject);
  }

  public toIntegrate(integration: IIntegration, index: number) {
    this.integrationNavigation.selectIntegration(integration);
    this.selectedIndex = index;
    this.router.navigate(['/configuration'], {relativeTo: this.route});
  }

  public toAnalyze(integration: IIntegration, index: number) {
    this.router.navigate(['/data-collection'], {relativeTo: this.route}).then(() => {
      this.integrationNavigation.selectIntegration(integration);
      this.selectedIndex = index;
    });
  }

  public addItem(): void {
    this.router.navigate(['/configuration'], {relativeTo: this.route}).then(() => {
      return this.integrationStore.createNewIntegration();
    }).then((integration: IIntegration) => {
      this.integrationNavigation.selectIntegration(integration);
    });
  }

  public sync(integration: IIntegration) {
    this.integrationNavigation.selectIntegration(integration);
    this.importer.createView(this.selectedIntegration._ids).subscribe(results => {
      this.logger.debug('Creating View', results);
    });
  }

  public async toGlobalSearch() {
    await this.router.navigate(['/search', {search: this.myForm.get(HomeComponent.input).value}], {relativeTo: this.route});
  }

  ngOnDestroy() {
    this.dataLoading = false;
    this.selectedIndex = 0;
  }
}

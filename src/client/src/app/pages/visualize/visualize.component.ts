import { Component, EventEmitter, OnInit } from '@angular/core';

import { Logger } from '../../classes/logger';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { ProxyService } from '../../services/proxy.service';
import { SearchDataService } from '../../services/search-data.service';

@Component({
  selector: 'app-visualize-page',
  templateUrl: './visualize.component.html',
  styleUrls: ['./visualize.component.scss']
})
export class VisualizeComponent implements OnInit {
  private logger = new Logger(this);

  private readonly NO_SAMPLES_MESSAGE = 'No samples found.';

  public title: string;
  public isLoading: boolean;
  public selectedData: any = [];

  public refreshTable = new EventEmitter();

  public from = 0;
  public size = 500;
  public emptyMessage;

  public selectedChart = 'Pivot';

  constructor(
    private elastic: ProxyService,
    private integrationNavigation: IntegrationNavigationService,
    private searchDataService: SearchDataService
  ) {
    this.isLoading = false;
  }

  ngOnInit() {
    this.isLoading = true;
    this.selectedData = [];
    this.searchDataService.data.forEach(value => this.selectedData.push(value));
    this.emptyMessage = this.NO_SAMPLES_MESSAGE;
    this.isLoading = false;

    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.title = integration ? integration.name : 'Empty/Invalid Dataset';
    });
  }

  public save() {
    throw new Error('Not Implemented');
  }
}

import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Logger } from '../../../classes/logger';
import { IReport, Report } from '../../../models/report';
import { ISearchEntry } from '../../../components/search-entry/search-entry';
import { IntegrationNavigationService } from '../../../services/integration-navigation.service';
import { ProxyService } from '../../../services/proxy.service';
import { SearchDataService } from '../../../services/search-data.service';
import { SearchService } from '../search.service';

@Component({
  selector: 'app-search-results',
  templateUrl: './search-results.component.html',
  styleUrls: ['./search-results.component.scss']
})
export class SearchResultsComponent implements OnInit, OnDestroy {
  private logger = new Logger(this);
  public queryValue = '';
  public searchEntries: ISearchEntry[] = [];

  public showResults = 'default';
  public dataLoading = false;

  private searchState: IReport;

  constructor(
    private elastic: ProxyService,
    private ref: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private searchService: SearchService,
    private searchData: SearchDataService,
    private integrationNav: IntegrationNavigationService
  ) {
    this.searchState = new Report();
  }

  ngOnInit() {
  }

  public selectEntry(entry: ISearchEntry) {
    this.searchService.searchEntry = entry;

    // This should get us the Data
    const report = new Report(null);
    report.IntegrationID = entry.id;
    report.Query = this.queryValue;
    this.searchState = new Report(report);
    this.integrationNav.selectIntegrationById(entry.id);
    this.router.navigate(['/data-collection']);
  }

  ngOnDestroy() {
    this.dataLoading = false;
  }
}

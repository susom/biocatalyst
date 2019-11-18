import { Component, EventEmitter, HostListener, OnInit, ViewChild } from '@angular/core';
import { DxDataGridComponent, DxListComponent } from 'devextreme-angular';

import { ConfigurationService } from '../../app.configuration';
import { IConfiguration } from '../../classes/configuration';
import { Logger } from '../../classes/logger';
import { Header } from '../../models/header';
import { IIntegration } from '../../models/integration';
import { IReport, Report } from '../../models/report';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { IntegrationStoreService } from '../../services/integration-store.service';
import { Network } from '../../services/network';
import { ElasticFieldDataTypes, ProxyService } from '../../services/proxy.service';
import { ReportNavigationService } from '../../services/report-navigation.service';
import { ReportStoreService } from '../../services/report-store.service';
import { ITypeInfo, SearchDataService } from '../../services/search-data.service';
import { SessionManager } from '../../services/session-manager.service';

@Component({
  selector: 'app-data-collection-table',
  templateUrl: './data-collection-table.component.html',
  styleUrls: ['./data-collection-table.component.scss']
})
export class DataCollectionTableComponent implements OnInit {
  private logger: Logger = new Logger(this);

  public integrations: IIntegration[];
  public integration: IIntegration;
  public allReports: IReport[];
  public reports: IReport[];
  public report: IReport;
  public columnList: any[];
  public countDisplay: string;
  public data: any[];
  public suggestions: any[];
  public gridOptionColumns: any[];

  private config: IConfiguration;

  @ViewChild(DxDataGridComponent, null) dataGrid: DxDataGridComponent;
  @ViewChild(DxListComponent, null) chooserList: DxListComponent;

  public selectedRows: number[];
  public showPopup = false;
  public showShare = false;

  public collapsedFilters = false;
  public scrolled = false;
  public cellElements = [];
  public dataSources = [];
  public sourceNames = [];

  public showHeaderFilter: boolean;
  public showFilter = false;

  public filterCreated: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private configurationService: ConfigurationService,
    private integrationStore: IntegrationStoreService,
    private integrationNavigation: IntegrationNavigationService,
    private reportStore: ReportStoreService,
    private reportNavigation: ReportNavigationService,
    private proxyService: ProxyService,
    private network: Network,
    private searchData: SearchDataService
  ) {
    this.configurationService.getConfiguration().then((config) => {
      this.config = config;
    });

    this.integrations = [];
    this.allReports = [];
    this.countDisplay = '';
    this.data = [];
    this.suggestions = [];

    this.showHeaderFilter = true;
  }

  public ngOnInit(): void {
    this.columnList = [];
    this.gridOptionColumns = [];

    this.setupIntegrations();
    this.setupReports();
    this.setupColumnUpdates();

    this.searchData.data.subscribe((data) => {
      this.data = data;
    });

    this.dataGrid.onOptionChanged.subscribe(value => {
      switch (value.name) {
        case 'columns':
          let exp: RegExp = new RegExp(/^columns\[\d*\].visible$/, 'm');
          exp = new RegExp(/^columns\[\d*\].visibleIndex$/, 'm');
          if (exp.test(value.fullName)) {
            this.handleOrderChange(value);
          }
          exp = new RegExp(/^columns\[\d*\].filterValues$/, 'm');
          if (exp.test(value.fullName)) {
            this.handleFilterChange(value);
          }
          break;
        case 'searchPanel':
          if (value.fullName === 'searchPanel.text' && value.value === '') {
            this.report.Query = '';
            this.searchData.refreshData();
          }
          break;
      }
    });
  }

  private setupIntegrations() {
    this.integration = this.integrationNavigation.getSelectedIntegration();

    this.integrationStore.integrationsUpdated.subscribe((integrations: IIntegration[]) => {
      this.integrations = integrations;
    });

    this.integrationStore.getIntegrations().catch((error) => this.logger.error(error));

    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.selectIntegration(integration);
    });
  }

  private setupReports() {
    this.reportStore.reportsUpdated.subscribe((reports) => {
      this.allReports = reports;
      this.filterReports();
    });

    this.reportStore.getReports().catch((error) => this.logger.error(error));

    this.reportNavigation.selectedReportChanged.subscribe((report) => {
      this.report = report;
    });
  }

  private setupColumnUpdates() {
    this.searchData.columns.subscribe(options => {
      if (Array.isArray(options) && options.length > 0) {
        this.sourceNames = [];
        this.cellElements = [];

        if (this.integration && this.integration.dataSources) {
          this.integration.dataSources.forEach(source => {
            this.sourceNames.push(source.name);
          });
        }

        if (this.report && this.report.Query) {
          this.dataGrid.instance.searchByText(this.report.Query);
        }

        const optionColumns = [];
        options.forEach((option) => {
          const originalName = option.name;
          const regex: RegExp = new RegExp(/^j\d+_s\d\d?_/);
          const match = regex.exec(originalName);
          const updatedLabel = match ? originalName.substring(match[0].length) : option.displayLabel;

          let visableColumn = new Header();
          if (this.report && this.report.VisableColumns.length > 0) {
            visableColumn = this.report.VisableColumns.find(header => header.Label === option.name);
          }

          optionColumns.push({
            dataField: option.name,
            caption: updatedLabel,
            allowSorting: true,
            allowColumnReordering: true,
            allowColumnResizing: true,
            visible: (visableColumn != null),
            visibleIndex: (visableColumn != null) ? visableColumn.Order : undefined,
            dataType: this.convertDataType(option.typeInfo),
            group: option.group
          });

          if (SessionManager.IsHiddenHeader(option.name)) {
            return;
          }
        });

        this.gridOptionColumns = optionColumns;

        const groups = optionColumns.reduce((obj, item) => {
          obj[item.group] = obj[item.group] || [];
          obj[item.group].push({text: item.caption, dataField: item.dataField});
          return obj;
        }, {});
        const myArray = Object.keys(groups).map(key => ({key, items: groups[key]}));
        this.columnList = myArray;

        this.dataGrid.instance.state(this.report ? this.report.State : 0);
      }
    });
  }

  private selectIntegration(integration: IIntegration): void {
    this.integration = integration;
    this.filterReports();
  }

  private filterReports(): void {
    if (this.integration && this.allReports) {
      const reports = this.allReports.filter((r) => r.IntegrationID === this.integration._ids);
      if (reports.length <= 0) {
        const report = new Report();
        report.Name = 'New Report';
        report.IntegrationID = this.integration._ids;
        this.reportStore.addReport(report);
      } else {
        this.reports = reports;
        this.reportNavigation.selectReport(this.reports[0]);
      }
    }
  }

  private convertDataType(typeInfo: ITypeInfo = {} as ITypeInfo) {
    let outType = 'string';
    switch (typeInfo.type) {
      case ElasticFieldDataTypes.Text:
      case ElasticFieldDataTypes.Keyword:
      case ElasticFieldDataTypes.Range:
      case ElasticFieldDataTypes.Unknown:
        outType = 'string';
        break;
      case ElasticFieldDataTypes.Numeric:
        outType = 'number';
        break;
      case ElasticFieldDataTypes.Date:
        outType = 'datetime';
        break;
      case ElasticFieldDataTypes.Binary:
        outType = 'boolean';
        break;
    }
    return outType;
  }

  public onStudyAndReportSelection(event): void {
    if (event.integration) {
      this.integrationNavigation.selectIntegration(event.integration);
    }
    if (event.report) {
      this.reportNavigation.selectReport(event.report);
    }
  }

  public saveReport(): void {
    this.report.State = this.dataGrid.instance.state();
    this.reportStore.saveToCloud(this.report).then(() => this.reportStore.getReports());
  }

  public getMoreDataLoop(): void {
    if (this.searchData.data.getValue().length < this.searchData.total) {
      this.searchData.fetchMore().then(() => {
        this.searchData.data.getValue().forEach(value => this.data.push(value));
        return this.getMoreDataLoop();
      });
    }
  }

  public handleOrderChange(value) {
    // Extract index
    const index = value.fullName.replace(/[^0-9]/g, '');

    // Get Column from State search Report
    const state = this.dataGrid.instance.state();
    const column = state.columns[index];

    const indexFound = this.report.VisableColumns.findIndex(header => {
      return header.Label === column.dataField;
    });

    if (indexFound < 0) {
      throw new Error('Value not found. This should never happen');
    }

    this.report.VisableColumns[indexFound].Order = value.value;
  }

  public handleFilterChange(value) {
    // Extract index
    const index = value.fullName.replace(/[^0-9]/g, '');

    // Get Column from State search Report
    const state = this.dataGrid.instance.state();
    const column = state.columns[index];

    if (!column) {
      throw new Error('column index not found. This shouldn\'t happen');
    }

    // Loop through array of filter values
    const valuesArray = value.value;
    if (valuesArray) {
      valuesArray.forEach(filterValue => {
        this.logger.debug('Adding filter', filterValue, column);
        // search for filter value in searchState Filters
        const indexFound = this.report.Filters.findIndex(header => {
          this.logger.debug('Check: filter change 2', header.Filter, filterValue, header.Filter === filterValue);
          return header.Filter === filterValue;
        });
        const newHeaders: Header[] = [];
        if (!value.previousValue) {
          let found: Header = null;
          // Find or create column
          if (indexFound < 0) {
            // Filtered headers will be visable.
            found = new Header({Order: 0, SortOrder: 0, Type: undefined, Label: column.dataField, Hidden: false, Filter: filterValue});
            newHeaders.push(found);
          } else {
            found = this.report.Filters[indexFound];
          }
          // Adjust column Attributes
          found.Hidden = false;
          found.Order = column.visibleIndex;
        } else if (indexFound >= 0) {
          this.report.Filters.splice(indexFound, 1);
        }
        this.report.Filters.push(...newHeaders);
      });
    }
  }

  public onTableReady(event) {
    this.dataSources = [];
    // Loop through each column
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < this.cellElements.length; i++) {
      // Add .highlight-s* class to column headers labelled
      const el = this.cellElements[i].element;
      const label = this.cellElements[i].dataField;

      const regex: RegExp = new RegExp(/s\d\d?_/);
      const match = regex.exec(label);

      if (match) {
        const sourceNumber = Number(match[0].charAt(1)) + 1; // extract number value from s0, s1, etc. and +1, so s0 = 1 and s1 = 2
        const closestCell = el.closest('.dx-datagrid-action');
        closestCell.classList.add('highlight-' + sourceNumber);
        if (!this.dataSources.includes(sourceNumber)) {
          this.dataSources.push(sourceNumber);
        }
      }
    }
  }

  // This gives us array of header cell elements so we can customize them
  onCellPrepared(e) {
    if (e.rowType === 'header' && e.column.dataField) {
      const dataField = e.column.dataField;
      const icon = e.cellElement;
      if (this.cellElements.length > 0) {
        let elIndex = -1;
        const el = this.cellElements.filter((c, index) => {
          if (c.dataField === dataField) {
            elIndex = index;
            return true;
          } else {
            return false;
          }
        });

        if (!el.length) {
          this.cellElements.push({element: icon, dataField});
        } else {
          this.cellElements[elIndex].element = icon;
        }
      } else {
        this.cellElements.push({element: icon, dataField});
      }
    }
  }

  onListContentReady() {
    this.chooserList.instance.selectAll();
  }

  onToolbarPreparing(e) {
    this.logger.debug('onToolbarPreparing', e);
    e.toolbarOptions.items.unshift({
        location: 'after',
        template: 'totalGroupCount'
      },
      {
        location: 'center',
        template: 'chips'
      },
      {
        location: 'before',
        template: 'integrationSelection'
      },
      {
        location: 'before',
        widget: 'dxButton',
        class: 'button-primary',
        options: {
          text: 'Save',
          icon: 'fa fa-save',
          onClick: () => {
            this.showPopup = true;
          }
        }
      },
      {
        location: 'after',
        widget: 'dxButton',
        options: {
          icon: 'fa fa-print',
          onClick: () => {
          }
        }
      },
      {
        location: 'after',
        widget: 'dxButton',
        options: {
          icon: 'fa fa-share-alt',
          onClick: () => {
            this.showShare = true;
          }
        }
      });
  }

  // When user scrolls toggle class so left filters stick to top of screen
  @HostListener('window:scroll', [])
  onWindowScroll() {
    const top = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
    if (top > 20) {
      this.scrolled = true;
    } else if (top < 10) {
      this.scrolled = false;
    }
  }

  public DisplayText(Amount: number, Total: number) {
    if (Amount > Total) {
      Amount = Total;
    }
    this.collapsedFilters = Amount < 1;
    const value = `${Amount} Results`;
    this.countDisplay = `${value}`;
  }

  public selectColumn(event) {
    this.logger.debug('selectColumn', event);

    const state = this.dataGrid.instance.state();
    const newHeaders: Header[] = [];

    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < event.addedItems.length; i++) {
      this.dataGrid.instance.columnOption(event.addedItems[i].dataField, 'visible', true);

      let found: Header = null;
      const indexFound = this.report.VisableColumns.findIndex(header => {
        this.logger.debug('Check: onOptionChanged 4', header.Label, event.addedItems[i].dataField,
          header.Label === event.addedItems[i].dataField);
        return header.Label === event.addedItems[i].dataField;
      });
      // Find or create column
      if (indexFound < 0) {
        // Newly added headers should always be visable.
        found = new Header({Filter: '', Order: 0, SortOrder: 0, Type: undefined, Label: event.addedItems[i].dataField, Hidden: false});
        newHeaders.push(found);
      } else {
        found = this.report.VisableColumns[indexFound];
      }
      // Adjust column Attributes
      found.Hidden = false;
    }
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < event.removedItems.length; i++) {
      this.dataGrid.instance.columnOption(event.removedItems[i].dataField, 'visible', false);
      const indexFound = this.report.VisableColumns.findIndex(header => {
        this.logger.debug('Check: onOptionChanged 4', header.Label, event.removedItems[i].dataField,
          header.Label === event.removedItems[i].dataField);
        return header.Label === event.removedItems[i].dataField;
      });
      if (indexFound >= 0) {
        this.logger.debug('Check: onOptionChanged splice 3', indexFound, this.report.VisableColumns);
        this.report.VisableColumns.splice(indexFound, 1);
      }
    }

    this.logger.debug('Check: onOptionChanged 6', newHeaders);
    this.report.VisableColumns.push(...newHeaders);
    this.dataGrid.instance.refresh();
  }

  toggleLeft() {
    this.collapsedFilters = !this.collapsedFilters;
  }

  width() {
    return window.outerWidth;
  }
}

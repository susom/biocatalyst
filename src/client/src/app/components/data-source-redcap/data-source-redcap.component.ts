import { AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Fade } from '../../animations/router-transition';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';
import { ColumnSelectorDropdown } from '../../models/column-selector-dropdown';
import { DataSource, IDataSource } from '../../models/data-source';
import { DropdownLayout } from '../../models/dropdown-layout';
import { DataSourceRedcapPipes } from './data-source-redcap.pipes';
import { DataSourceRedcapService } from './data-source-redcap.service';

export enum URLREQUESTSTATE {
  'Default' = 0,
  'Requesting' = 1,
  'Returned' = 2
}

@Component({
  selector: 'app-data-source-redcap-form',
  templateUrl: './data-source-redcap.component.html',
  styleUrls: ['./data-source-redcap.component.scss'],
  animations: [Fade],
  providers: [DataSourceRedcapService, DataSourceRedcapPipes]
})
export class DataSourceRedcapComponent implements OnInit, OnChanges, AfterViewInit {
  @Input() source: DataSource = null;
  @Input() sources: IDataSource[] = [];
  @Input() sourceTypeChange = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();
  @Output() tokens: EventEmitter<string[]> = new EventEmitter<string[]>();
  private logger = new Logger(this);

  public urlrequeststateenum = URLREQUESTSTATE;

  public oSource = new BehaviorSubject<IDataSource>(null);
  public projectId = new BehaviorSubject<string>('');
  public reportId = new BehaviorSubject<string>('');
  public projectReportColumns = new BehaviorSubject<KeyValue<string>[]>(null);
  public connectionDetailsColumnDropdownLayout: DropdownLayout[] = [];
  public processingSource = new BehaviorSubject<boolean>(false);

  public projectsColumnDropdownLayout: DropdownLayout = new DropdownLayout({details: new ColumnSelectorDropdown({
    label: 'Project Id',
    defaultValue: null,
    columns: [],
    scssClass: 'ui-g-12',
    onSelection: (result: any) => {
      this.projectId.next(result);
    }
  })});

  public reportsColumnDropdownLayout: DropdownLayout = new DropdownLayout({details: new ColumnSelectorDropdown({
    label: 'Report Id',
    defaultValue: null,
    columns: [],
    scssClass: 'ui-g-12',
    onSelection: (result: any) => {
      this.reportId.next(result);
    }
  })});

  constructor(
    private ref: ChangeDetectorRef,
    public dataSourceRedcapService: DataSourceRedcapService,
    private dataSourceRedcapPipes: DataSourceRedcapPipes) {
  }

  ngOnInit() {
    this.dataSourceRedcapService.TriggerGetProjects();
    this.connectionDetailsColumnDropdownLayout.push(this.projectsColumnDropdownLayout);
    this.RegisterPipes();
    this.ProcessSource();
    this.UpdateLayout();
  }

  ngAfterViewInit() {
    this.source.connection_details.URL = null;
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      if (propName === 'source') {
        if (!this.source) {
          return;
        }
        this.ProcessSource();
        this.UpdateLayout();
        this.ref.detectChanges();
      }
    }
  }

  private RegisterPipes() {
    // Registering Pipes
    this.projectId.pipe(
      this.dataSourceRedcapPipes.GetReportsBasedOffProjectID(this)
    ).subscribe();

    this.dataSourceRedcapService.Projects$.pipe(
      this.dataSourceRedcapPipes.GenerateAndUpdateProjectLabels(this)
    ).subscribe();

    this.reportId.pipe(
      this.dataSourceRedcapPipes.GetColumnsBasedOfReportID(this)
    ).subscribe();

    this.dataSourceRedcapService.Reports$.pipe(
      this.dataSourceRedcapPipes.GenerateAndUpdateReportLabels(this)
    ).subscribe();

    this.dataSourceRedcapService.Columns$.pipe(
      this.dataSourceRedcapPipes.GenerateAndUpdateReportColumnLabels(this)
    ).subscribe();

    this.sourceTypeChange.pipe(
      this.dataSourceRedcapPipes.ResetTokens(this)
    ).subscribe();
  }

  private ProcessSource() {
    this.processingSource.next(true);

    if (this.source) {
      if (this.source.connection_details.credentials == null) {
        this.source.connection_details.credentials = {
          password: null,
          user: null
        };
      }
      this.projectId.next(this.source.connection_details.data);
      this.reportId.next(this.source.connection_details.credentials.password);
    }
    this.oSource.next(this.source);
    this.processingSource.next(false);

  }

  private UpdateLayout() {
    this.projectsColumnDropdownLayout.details.defaultValue = this.projectId.getValue();
    this.reportsColumnDropdownLayout.details.defaultValue = this.reportId.getValue();
  }

  // Trigger Change
  public TriggerChange(event) {
    this.dataSourceRedcapPipes.TriggerChanges(this);
  }
}

import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BehaviorSubject } from 'rxjs';
import { distinctUntilChanged, filter, switchMap, tap } from 'rxjs/operators';
import { Fade } from '../../animations/router-transition';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';
import { ColumnSelectorDropdown } from '../../models/column-selector-dropdown';
import { DataSource, IDataSource } from '../../models/data-source';
import { IntegrationConnection } from '../../models/integration-connections';
import { ISubjectCodeTypes, TokenizationInstructions } from '../../models/tokenization-instructions';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { Network } from '../../services/network';
import { ProxyService } from '../../services/proxy.service';
import { ServerEnvironmentService } from '../../services/server-environment.service';
import { EpicUIColumn, EpicUiModel, IEpicUIColumn } from './epic-ui-model';

export enum URLREQUESTSTATE {
  'Default' = 0,
  'Requesting' = 1,
  'Returned' = 2
}

@Component({
  selector: 'app-data-source-epic-form',
  templateUrl: './data-source-epic.component.html',
  styleUrls: ['./data-source-epic.component.scss'],
  animations: [Fade]
})
export class DataSourceEpicComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  @Input() source: DataSource = null;
  @Input() sources: IDataSource[] = [];
  @Input() sourceTypeChange = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();

  private get IRB(): string {
    if (!this.source.connection_details.data) {
      return null;
    }
    if (!this.source.connection_details.data.irb) {
      return null;
    }
    return this.source.connection_details.data.irb;
  }

  public data_types: any[] = [];

  public getIRBs = new EventEmitter<any>();
  public irbs = [];
  public gettingIRBsStatus = new BehaviorSubject<URLREQUESTSTATE>(URLREQUESTSTATE.Default);
  public gettingIRBs = new BehaviorSubject<boolean>(false);
  public retrievedIRBs = new BehaviorSubject<boolean>(false);

  public DATASOURCE_NAMES: KeyValue<string>[] = [];

  public irbSUB = this.getIRBs.pipe(
    tap(value => {
      this.logger.debug('getIRBs');
      this.gettingIRBs.next(true);
      this.gettingIRBsStatus.next(URLREQUESTSTATE.Requesting);
      this.retrievedIRBs.next(false);
      this.irbs = [];
    }), switchMap(value => {
      return this.biosearchProxy.getStrideIRBs();
    })).subscribe(value => {
    this.logger.debug(value);
    this.gettingIRBs.next(false);
    this.retrievedIRBs.next(true);
    const response = JSON.parse(value);
    const protocols = response.protocols;
    const irbs = [];
    for (const protocol of protocols) {
      irbs.push({
        protocol: protocol.protocolNumber,
        protocolTitle: protocol.protocolTitle
      });
    }
    this.irbs = irbs;
  });

  public irbNumber = '';
  public getMetadata = new EventEmitter<any>();
  public getMetadataStatus = new BehaviorSubject<URLREQUESTSTATE>(URLREQUESTSTATE.Default);
  public gettingMetadata = new BehaviorSubject<boolean>(false);
  public getMetadatasub = this.getMetadata.pipe(
    distinctUntilChanged(),
    tap(value => {
      this.data_types = [];
    }),
    filter(value => value != null),
    tap(value => {
      this.gettingMetadata.next(true);
      this.getMetadataStatus.next(URLREQUESTSTATE.Requesting);
      this.irbNumber = value;
      // Update for The irb number
      this.updateSource(value, []);
      this.ref.detectChanges();
    }),
    switchMap(value => {
      return this.biosearchProxy.getStrideMetadata(value);
    }))
    .subscribe((value: any) => {
      this.logger.debug('getMetadata', (value));

      if (value.hasOwnProperty('error')) {
        return;
      }

      const response = JSON.parse(value);

      for (const dataType in response) {
        const model = new EpicUiModel(dataType, false, null, 'test items	', []);
        const columns = response[dataType];
        for (const column of columns) {
          model.columns.push(new EpicUIColumn(column.label, column.variable_name, false));
        }

        this.data_types.push(model);
      }

      this.gettingMetadata.next(false);
      this.getMetadataStatus.next(URLREQUESTSTATE.Returned);

      // Update for The Data
      this.updateSource(this.irbNumber, this.data_types);
    });

  public dataSources = [];
  public columnSelector: ColumnSelectorDropdown;

  constructor(
    private ref: ChangeDetectorRef,
    private dialog: MatDialog,
    private network: Network,
    private serverEnvironment: ServerEnvironmentService,
    private integrationNav: IntegrationNavigationService,
    private biosearchProxy: ProxyService) {
  }

  ngOnInit() {
    this.getIRBs.emit();

    this.integrationNav.selectedIntegrationChanged.subscribe(([integration, index]) => {
      integration.dataSources.forEach((source: IDataSource) => {
        if (source.source_id !== this.source.source_id) {
          this.dataSources.push({name: source.name, id: source.source_id});
        }
      });
    });

    // Hard code the Subject Code becuase it needs to be this no matter what
    const hardInstructions: TokenizationInstructions = new TokenizationInstructions({
      type: ISubjectCodeTypes.mrn,
      columnLabel: 'Participant MRN Column Name',
      columnName: 'suid', // Can be hardcoded in the backed
      delimiter_positions: [],
      important_tokens: [],
      important_tokens_order: [],
      processed_id: null,
      configurableType: false,
      configurableColumn: false,
      removeable: false,
      growth_tokens: [],
      format: '',
      format_moment: ''
    });

    if (this.source.integration_details.subjectCode.length <= 0) {
      this.source.integration_details.subjectCode.push(hardInstructions);
    } else {
      this.source.integration_details.subjectCode[0] = hardInstructions;
    }
    // Hard code the Subject Code becuase it needs to be this no matter what
    const hardConnection: IntegrationConnection = new IntegrationConnection({
      sourceId: '0',
      subjectCodeType: ISubjectCodeTypes.mrn,
      matching_strategy: '',
      visitId: undefined
    });

    if (this.source.integration_details.subjectCode.length <= 0) {
      this.source.integration_details.connections.push(hardConnection);
    } else {
      this.source.integration_details.connections[0] = hardConnection;
    }
    this.source.connection_details.credentials = null;
    this.source.connection_details.URL = null;
    // Initialize Data
    if (this.source.connection_details) {
      if (this.source.connection_details.data) {
        this.irbNumber = this.source.connection_details.data.irb;
        for (const column of this.source.connection_details.data.columns) {
          const model = new EpicUiModel();
          model.Initialize(column);
          this.data_types.push(model);
        }
      }
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      if (propName === 'source') {
        this.processSource();
        this.ref.detectChanges();
      }
    }
  }

  private processSource(): void {
    // Generate Source Data Names
    this.DATASOURCE_NAMES = [];
    for (const source of this.sources) {
      if (source.primary) {
        // Only allow connections to the primary
        this.DATASOURCE_NAMES.push(new KeyValue<string>(source.name, source.source_id));

        this.columnSelector = new ColumnSelectorDropdown({
          label: 'Connect to...',
          defaultValue: this.DATASOURCE_NAMES[0].value,
          columns: this.DATASOURCE_NAMES,
          scssClass: '',
          onSelection: (result) => {
            this.source.integration_details.connections[0].sourceId = result;
            this.onChange.emit();
          }
        });
      }
    }
  }

  selectAll(e) {
    // 0 - Event
    // 		event.value = current value
    // 1 - Data , array columns
    this.logger.debug('selectAll', e);
    const event = e[0];
    const data = e[1] as EpicUiModel;
    if (event.event) {
      event.event.stopPropagation();
      data.columns.forEach((column: any) => column.integration = event.value);
      data.itemsSelected = (event.value) ? data.columns.length : 0;
      data.updateDataText();
    }
    this.updateSource(this.IRB, this.data_types);
  }

  private updateSource(IRB: string, Columns: IEpicUIColumn[]) {
    this.source.connection_details.data = {
      irb: IRB,
      columns: Columns
    };
    this.onChange.emit();
  }

  columnSelected(e) {
    // 0 - Event
    // 		event.value = current value
    // 1 - Data Column
    // 2 - column.integration
    this.logger.debug('columnSelected', e);

    const event = e[0];
    const data = e[1] as EpicUiModel;
    if (event) {
      (event.value) ? ++data.itemsSelected : --data.itemsSelected;
      data.updateDataText();
    }
    this.updateSource(this.IRB, this.data_types);
  }
}

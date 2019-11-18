import { animate, state, style, transition, trigger } from '@angular/animations';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';
import { DialogConfig } from '../../dialogs/dialog-config';
import { ColumnSelectorDropdown } from '../../models/column-selector-dropdown';
import { EventMappingData } from '../../models/event-mapping-table';
import { InjectableEventMappingTableObject } from '../../models/injectable-event-mapping-table-object';
import { IntegrationConnectionVisitIdData } from '../../models/integration-connection-form-data';
import { IIntegrationConnection, IntegrationConnection, IntegrationConnectionVisitId } from '../../models/integration-connections';
import {
  ISubjectCodeTypes,
  ITokenizationInstructions,
  IVisitIdTypes,
  TokenizationInstructions
} from '../../models/tokenization-instructions';
import { DataSourceService } from '../../services/data-source.services';
import { ModalDialogService } from '../../services/modal-dialog.service';
import { EventMappingTableComponent } from '../event-mapping-table/event-mapping-table.component';

@Component({
  selector: 'app-integration-connection',
  templateUrl: './integration-connection.component.html',
  styleUrls: ['./integration-connection.component.scss'],
  animations: [
    trigger('showState', [
      state('shown', style({})),
      transition(':enter', [
        style({
          opacity: 0
        }),
        animate('100ms 500ms')
      ]),
      transition(':leave',
        animate(300, style({
          opacity: 0
        }))
      )
    ])
  ]
})
export class IntegrationConnectionComponent implements OnInit, OnChanges {
  private logger = new Logger(this);
  @Input() columns: KeyValue<string>[] = [];

  @Input() tokenizationInstructions: ITokenizationInstructions = null;
  @Input() integrationConnection: IIntegrationConnection = null;
  @Input() defaultConnections: IIntegrationConnection[] = [];

  @Input() sourceName = '';
  @Input() sourceIdNames: KeyValue<string>[] = []; // Needs to be a Key Label pair
  @Input() subjectTokenizationInstructions: ITokenizationInstructions[] = [];
  @Input() visitTokenizationInstructions: ITokenizationInstructions[] = [];
  @Input() refresh: EventEmitter<any> = new EventEmitter<any>();
  @Input() launchModalTrigger: EventEmitter<any> = new EventEmitter<any>();

  @Output() onChangeFormData: EventEmitter<any> = new EventEmitter<any>();
  @Output() onIntentRemove: EventEmitter<any> = new EventEmitter<any>();

  private triggerOnChange: EventEmitter<any> = new EventEmitter<any>();
  private triggerOnChangeChain: Observable<any> = this.triggerOnChange.pipe(
    tap(value => {
      const connection = this.GenerateConnectionDetails();
      this.onChangeFormData.emit(connection);
    })
  );

  private selectedSubjectType: BehaviorSubject<ISubjectCodeTypes> = new BehaviorSubject<ISubjectCodeTypes>(null);
  private selectedConnectionType: BehaviorSubject<IVisitIdTypes> = new BehaviorSubject<IVisitIdTypes>(null);
  private selectedConnectionTypeChain = this.selectedConnectionType.pipe(
    tap(value => {
      this.visitData.type = value;

      switch (value) {
        case IVisitIdTypes.visitLabel:
        case IVisitIdTypes.visitCode:
        case IVisitIdTypes.other:
          this.showMappingUI.next(true);
          break;
        default:
          this.showMappingUI.next(false);
          break;
      }

    })
  );

  private mapping: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  private mappingChain = this.mapping
    .pipe(
      tap(value => {
        this.visitData.eventNameMapping = value;
      })
    );

  private showMappingUI: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private connectingSourceName: BehaviorSubject<string> = new BehaviorSubject<string>(null);

  public integrationDetails: ColumnSelectorDropdown[] = [];

  public visitData: IntegrationConnectionVisitIdData = new IntegrationConnectionVisitIdData();
  public tokenizationData: TokenizationInstructions = new TokenizationInstructions();

  // Column Selector Dropdown Handles
  public participantID: ColumnSelectorDropdown = null;
  public sourceID: ColumnSelectorDropdown = null;
  public visitID: ColumnSelectorDropdown = null;

  constructor(
    private ref: ChangeDetectorRef,
    private myDataSourceService: DataSourceService,
    private modal: ModalDialogService) {
  }

  ngOnInit() {
    // initialize Form Data
    this.integrationDetails = [];
    this.GenerateDetails();
    this.mappingChain.subscribe();
    this.selectedConnectionTypeChain.subscribe();
    this.triggerOnChangeChain.subscribe();

    this.refresh.subscribe(() => {
      this.GenerateDetails();
      this.triggerOnChange.emit();
    });

    this.launchModalTrigger.subscribe(() => {
      this.luanchMappingModal();
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.subjectTokenizationInstructions) {
      this.GenerateParticipantID();
    }
    if (changes.visitTokenizationInstructions) {
      this.GenerateVisitID();
    }
    if (changes.sourceIdNames) {
      this.GenerateSourceID();
    }
    if (changes.defaultConnections) {
      if (this.defaultConnections[0]) {
        if (this.defaultConnections[0].visitId) {
          if (this.defaultConnections[0].visitId.eventNameMapping) {
            this.mapping.next(this.defaultConnections[0].visitId.eventNameMapping);
          }
        }
      }
    }
  }

  public dateMatchingRangeInDays() {
    if (this.defaultConnections[0]) {
      if (this.defaultConnections[0].visitId) {
        return this.defaultConnections[0].visitId.dateMatchingRangeInDays;
      }
    }
    return 0;
  }

  public GenerateDetails() {
    this.GenerateParticipantID();
    this.GenerateSourceID();
    this.GenerateVisitID();
  }

  private generateDetails(
    label: string,
    defaultValue: string = null,
    columns: KeyValue<string>[] = null,
    onSelection: (result: any) => any = (result: any) => {
      this.logger.debug(label + ' - Selection:', result);
    }) {
    return new ColumnSelectorDropdown({label, defaultValue, columns, scssClass: null, onSelection});
  }

  private GenerateParticipantID() {
    if (!this.subjectTokenizationInstructions) {
      return;
    }
    const instruction = this.subjectTokenizationInstructions[0]; // Default to First for now.
    const defaultsubjectToken = (instruction) ? instruction.columnName : null;
    const options = this.myDataSourceService.extractColumnNames(this.subjectTokenizationInstructions);
    this.participantID = this.generateDetails(
      'Participant ID',
      defaultsubjectToken, // <--- Populate Default Value
      options,
      (result: any) => {
        this.logger.debug(name + ' - Selection:', result);
        this.tokenizationData.columnName = result;
      });

    this.selectedSubjectType.next(instruction.type as ISubjectCodeTypes);
  }

  private GenerateSourceID() {
    const defaultSourceID = (this.sourceIdNames[0]) ? this.sourceIdNames[0].value : null;
    const defaultSourceName = (this.sourceIdNames[0]) ? this.sourceIdNames[0].key : null;
    this.connectingSourceName.next(defaultSourceName);
    this.sourceID = this.generateDetails(
      'Source',
      defaultSourceID, // <--- Populate Default Value
      this.sourceIdNames,
      (result: any) => {
        this.logger.debug(name + ' - Selection:', result);
        this.connectingSourceName.next(name);
      });
  }

  private GenerateVisitID() {
    if (!this.visitTokenizationInstructions) {
      return;
    }
    const instruction = this.visitTokenizationInstructions[0]; // Default to First for now.
    const defaultVisitToken = (instruction) ? instruction.columnName : null;
    const options = this.myDataSourceService.extractColumnNames(this.visitTokenizationInstructions);
    this.visitID = this.generateDetails(
      'Visit ID',
      defaultVisitToken, // <--- Populate Default Value
      options,
      (result: any) => {
        this.logger.debug(name + ' - Selection:', result);
        this.visitData.sourceId = result;
      });

    const defaultConnectionType = (instruction) ? instruction.type as IVisitIdTypes : null;
    this.selectedConnectionType.next(defaultConnectionType);
  }

  private GenerateConnectionDetails() {
    const connection = new IntegrationConnection();

    // Hack. Taking from position 0. Need a way to identify each subject code
    connection.sourceId = '0';
    connection.subjectCodeType = this.selectedSubjectType.getValue() as ISubjectCodeTypes;

    connection.visitId = null;
    if (this.visitTokenizationInstructions.length > 0) {
      connection.visitId = new IntegrationConnectionVisitId();
      connection.visitId.dateMatchingRangeInDays = this.visitData.dateMatchingRangeInDays;
      connection.visitId.eventNameMapping = this.visitData.eventNameMapping;
      connection.visitId.type = this.visitData.type;
    }

    return connection;
  }

  public luanchMappingModal() {
    const injectable = new InjectableEventMappingTableObject();

    let mapping = this.mapping.getValue();
    if (!mapping) {
      mapping = new EventMappingData();
    }
    injectable.source = mapping.source;
    injectable.destination = mapping.destination;
    injectable.sourceName = this.sourceName;
    injectable.connectingSource = this.connectingSourceName.getValue();
    injectable.mapping = mapping.mapping;
    injectable.headers = mapping.headers;
    injectable.onChange.pipe(
      tap(value => {
        this.onEventMappingChange(value);
      })
    ).subscribe();
    const Config: DialogConfig = new DialogConfig('80%', '80%', EventMappingTableComponent, injectable);
    this.modal.open(Config);
  }

  public visitDateModifier(value: any) {
    this.logger.debug('visitDateModifier - Altered:', value);
    this.visitData.dateMatchingRangeInDays = value;
    this.triggerOnChange.emit();
  }

  public onEventMappingChange(value: any) {
    this.logger.debug('onEventMappingChange - Altered:', value);
    this.mapping.next(value);
    this.triggerOnChange.emit();
  }

  public get VisitTokenInstructionCount() {
    return this.visitTokenizationInstructions.length;
  }
}

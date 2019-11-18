import { KeyValue } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { Fade } from '../../animations/router-transition';
import { Logger } from '../../classes/logger';
import { DialogConfig } from '../../dialogs/dialog-config';
import { ColumnSelectorDropdown } from '../../models/column-selector-dropdown';
import { DataSource, IDataSource, SourceType } from '../../models/data-source';
import { DropdownLayout } from '../../models/dropdown-layout';
import { IIntegration, Integration } from '../../models/integration';
import { IIntegrationConnection, IntegrationConnection } from '../../models/integration-connections';
import {
  ISubjectCodeTypes,
  ITokenizationInstructions,
  IVisitIdTypes,
  TokenizationInstructions
} from '../../models/tokenization-instructions';
import { DataSourceService } from '../../services/data-source.services';
import { IntegrationStoreService } from '../../services/integration-store.service';
import { ModalDialogService } from '../../services/modal-dialog.service';
import { DateFormatConfiguration } from '../date-format-configuration/date-format-configuration';
import { DateFormatConfigurationComponent } from '../date-format-configuration/date-format-configuration.component';

@Component({
  selector: 'app-data-source-template-form',
  templateUrl: './data-source-template.component.html',
  styleUrls: ['./data-source-template.component.scss'],
  animations: [Fade]
})
export class DataSourceTemplateComponent implements OnChanges {
  private logger = new Logger(this);

  @Input() integration: IIntegration;
  @Input() dataSource: IDataSource;
  @Input() dataSources: IDataSource[] = [];
  @Input() defaultOptions: string[];
  @Output() dataSourceChange: EventEmitter<any>;

  public options: KeyValue<string, string>[];
  public dataSourceNames: KeyValue<string, string>[];
  public subjectIdLayouts: DropdownLayout[];
  public visitIdLayouts: DropdownLayout[];

  private saveEvent: EventEmitter<any>;

  public triggerLaunchEditMappingModal: EventEmitter<any>;
  public triggerColumnLayoutSaveError: EventEmitter<any>;
  public triggerConnectionDetailsRefresh: EventEmitter<any>;

  constructor(private dataSourceService: DataSourceService,
              private integrationStore: IntegrationStoreService,
              private modal: ModalDialogService) {
    this.integration = new Integration();
    this.dataSource = new DataSource();
    this.dataSources = [];
    this.defaultOptions = [];
    this.dataSourceChange = new EventEmitter<any>();

    this.options = [];
    this.dataSourceNames = [];
    this.subjectIdLayouts = [];
    this.visitIdLayouts = [];

    this.saveEvent = new EventEmitter<any>();

    this.triggerLaunchEditMappingModal = new EventEmitter<any>();
    this.triggerColumnLayoutSaveError = new EventEmitter<any>();
    this.triggerConnectionDetailsRefresh = new EventEmitter<any>();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.dataSources) {
      this.dataSourceNames = this.dataSources.filter((source) => source.primary)
        .map((source) => ({key: source.source_id, value: source.name} as KeyValue<string, string>));
    }
    if (changes.dataSource) {
      this.initializeSource();
      this.initializeSubscriptions();
      this.processLayout();
      this.triggerConnectionDetailsRefresh.emit();
    }
    if (changes.defaultOptions) {
      this.updateOptions();
    }
  }

  private initializeSource() {
    // Initialize subject code in case it was empty
    this.dataSourceService.setDefaultSubjectCodesIfAble(this.dataSource.integration_details.subjectCode, this.dataSource.primary);
    this.dataSourceService.setDefaultVisitCodesIfAble(
      this.dataSource.integration_details.visitId,
      this.dataSource.primary,
      this.dataSource.type as SourceType);
    this.dataSourceService.makePPIDConfigurable(this.dataSource.integration_details.subjectCode);

    // Ensure at least One Connection if it's not already there.
    if (this.dataSource.integration_details.connections.length === 0) {
      this.dataSource.integration_details.connections.push(new IntegrationConnection());
    }
  }

  private updateOptions() {
    if (this.dataSource) {
      this.options = this.defaultOptions.map((option: string) => ({key: option, value: option} as KeyValue<string, string>));
    }
  }

  private processLayout() {
    this.GenerateSubjectIDLayouts();
    this.GenerateVisitIDLayouts();

    // There should only be One Visit Id for our current system to run properly
    // Likewise, there should be only One VisitID and One Connection. They map together for now.
    // in the event of a non primary
    if (!this.dataSource.primary) {
      if (this.dataSource.integration_details.connections.length > 1) {
        this.logger.debug(
          'Severe Error: Can\'t have more than one Connection',
          this.dataSource.integration_details.connections.length
        );
        throw new Error('Severe Error: Can\'t have more than one Connection');
      }
      if (this.dataSource.integration_details.visitId.length > 1) {
        this.logger.debug(
          'Severe Error: Can\'t have more than one visitID',
          this.dataSource.integration_details.visitId.length
        );
        throw new Error('Severe Error: Can\'t have more than one visitID');
      }
    }

  }

  private initializeSubscriptions() {
    this.saveEvent.subscribe((values: any) => {
      this.dataSource.integration_details.subjectCode.forEach(detail => {
        if (detail.type === ISubjectCodeTypes.ppid) {
          detail.delimiter_positions = values.delimiter_positions;
          detail.important_tokens = values.important_tokens;
          detail.processed_id = values.processed_id;
          detail.important_tokens_order = values.important_tokens_order;
          detail.growth_tokens = values.growth_tokens;
        }
      });

      this.dataSourceChange.emit(this.dataSource);
    });
  }

  private GenerateSubjectIDLayouts() {
    this.subjectIdLayouts = [];

    this.dataSource.integration_details.subjectCode.forEach((instructions: TokenizationInstructions) => {
      const layout = new DropdownLayout();
      let spacing = 0;
      const scssSpacing = (instructions.type == null) ? 'ui-g-6' : 'ui-g-12';

      layout.data = instructions;

      if (instructions.removeable) {
        spacing += 1;
        layout.removeable = true;
        layout.onRemove = r => this.subjectIdLayouts = this.subjectIdLayouts.filter((l) => l !== r);
      }

      // Generate Initial Dropdown
      layout.details = new ColumnSelectorDropdown({
        label: instructions.columnLabel,
        defaultValue: instructions.columnName,
        columns: null,
        scssClass: scssSpacing,
        onSelection: (result: any) => {
          instructions.columnName = result;
          this.dataSourceChange.emit(this.dataSource);
        }
      });

      spacing += 1;

      // Adding Type Selection.

      let types = this.dataSourceService.SubjectCodeTypes;

      const determineColumnConfigurationLayout = (type: any) => {
        layout.hideColumnConfiguration = !type;
        switch (type) {
          case IVisitIdTypes.visitLabel:
          case IVisitIdTypes.visitCode:
          case IVisitIdTypes.other:
            layout.showAlternateColumnConfiguration = false;
            break;
          case undefined:
            layout.showAlternateColumnConfiguration = undefined;
            break;
          default:
            layout.showAlternateColumnConfiguration = true;
            if (false) {
              layout.hideColumnConfiguration = false;
              types = [{key: IVisitIdTypes.visitDate, value: IVisitIdTypes.visitDate}];
            }
            break;
        }
      };

      determineColumnConfigurationLayout(instructions.type);

      if (instructions.configurableType) {
        layout.type = new ColumnSelectorDropdown({
          label: instructions.columnLabel + ' Type',
          defaultValue: instructions.type,
          columns: types,
          scssClass: scssSpacing,
          onSelection: (result: any) => {
            instructions.type = result;
            determineColumnConfigurationLayout(result);
          }
        });

        spacing += 1;
      }

      layout.configureColumn = instructions.configurableColumn;
      if (layout.configureColumn) {
        spacing += 1;
      }
      layout.setSpacing(spacing);

      this.subjectIdLayouts.push(layout);
    });
  }

  public GenerateVisitIDLayouts() {
    this.visitIdLayouts = [];

    this.dataSource.integration_details.visitId.forEach((instructions: TokenizationInstructions) => {
      const layout = new DropdownLayout();
      let spacing = 0;
      const scssSpacing: string = (instructions.type == null) ? 'ui-g-6' : 'ui-g-12';

      layout.data = instructions;

      if (instructions.removeable) {
        spacing++;
        layout.removeable = true;
        layout.onRemove = r => this.visitIdLayouts = this.visitIdLayouts.filter((l) => l !== r);
      }

      layout.details = new ColumnSelectorDropdown({
        label: instructions.columnLabel,
        defaultValue: instructions.columnName,
        columns: null,
        scssClass: scssSpacing,
        onSelection: (result: any) => {
          instructions.columnName = result;
        }
      });

      // Disable configuraiton on Empty columns
      if (instructions.columnName == null) {
        layout.disableColumnConfiguration = true;
      }

      spacing += 1;

      // Adding Type Selection.

      let types = this.dataSourceService.VisitIdTypes;

      const determineColumnConfigurationLayout = (type: any) => {
        layout.hideColumnConfiguration = (type) ? false : true;
        switch (type) {
          case IVisitIdTypes.visitLabel:
          case IVisitIdTypes.visitCode:
          case IVisitIdTypes.other:
            layout.showAlternateColumnConfiguration = false;
            break;
          case undefined:
            layout.showAlternateColumnConfiguration = undefined;
            break;
          default:
            // If visitDate
            layout.showAlternateColumnConfiguration = true;
            if (this.dataSource.primary) {
              layout.hideColumnConfiguration = false;
              types = [{key: IVisitIdTypes.visitDate, value: IVisitIdTypes.visitDate} as KeyValue<string, string>];
            }
            break;
        }
      };

      determineColumnConfigurationLayout(instructions.type);

      if (instructions.configurableType) {
        layout.type = new ColumnSelectorDropdown({
          label: instructions.columnLabel + ' Type',
          defaultValue: instructions.type,
          columns: types,
          scssClass: scssSpacing,
          onSelection: (result: any) => {
            instructions.type = result;
            determineColumnConfigurationLayout(result);
          }
        });

        spacing += 1;
      }

      layout.configureColumn = instructions.configurableColumn;
      if (layout.configureColumn) {
        spacing += 1;
      }
      layout.setSpacing(spacing);

      this.visitIdLayouts.push(layout);
    });
  }

  public integrationConnectionUpdate(data: IIntegrationConnection) {
    this.dataSource.integration_details.connections[0] = data;
    this.dataSourceChange.emit(this.dataSource);
  }

  public visitDateMatchingRangeInDays() {
    try {
      return this.dataSource.integration_details.connections[0].visitId.dateMatchingRangeInDays;
    } catch {
      return 0;
    }
  }

  public visitDateModifier(value: any) {
    this.dataSource.integration_details.connections[0].visitId.dateMatchingRangeInDays = value;
    this.dataSourceChange.emit(this.dataSource);
  }

  checkTokenProcessed(csvLayout: DropdownLayout) {
    const instruction = csvLayout.data as ITokenizationInstructions;
    if (instruction) {
      if (instruction.processed_id) {
        return instruction.processed_id.length > 0;
      }
    }
    return false;
  }

  configureSubjectID(csvLayout: DropdownLayout) {
    csvLayout.disableColumnConfiguration = true;
    this.launchVerifyModal(csvLayout.data).subscribe((values) => {
      this.dataSourceChange.emit(this.dataSource);
      csvLayout.disableColumnConfiguration = false;
    });
  }

  configureDateFormat(csvLayout: DropdownLayout) {
    this.launchDateFormattingModal(csvLayout.data);
  }

  public launchVerifyModal(tokenInstructions: ITokenizationInstructions): Observable<any> {
    return this.dataSourceService.verifyModal(
      this.integration._ids,
      this.dataSource.source_id,
      this.saveEvent,
      this.saveEvent,
      tokenInstructions.important_tokens,
      tokenInstructions.important_tokens_order,
      tokenInstructions.processed_id,
      this.dataSource.primary,
      tokenInstructions.delimiter_positions
    );
  }

  public launchDateFormattingModal(tokenInstructions: ITokenizationInstructions) {
    const injectable = new DateFormatConfiguration();
    injectable.configuration = this.integration._ids;
    injectable.sourceId = this.dataSource.source_id;
    injectable.defaultValue = tokenInstructions.format_moment;
    injectable.onChange.subscribe(value => {
      tokenInstructions.format_moment = value;
      tokenInstructions.format = DateFormatConfiguration.ConvertMomentFormatToBackendFormat(value);
    });
    const Config: DialogConfig = new DialogConfig('80%', '80%', DateFormatConfigurationComponent, injectable);
    this.modal.open(Config);
  }

  addVisit() {
      this.dataSource.integration_details.visitId.push(
        new TokenizationInstructions({
          type: null,
          columnLabel: 'Specimen Visit ID',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: true,
          configurableColumn: true,
          removeable: true
        })
      );
      this.GenerateVisitIDLayouts();
  }
}

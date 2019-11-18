import { KeyValue } from '@angular/common';
import { EventEmitter, Injectable } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material';
import { BehaviorSubject } from 'rxjs';
import { Logger } from '../classes/logger';
import { VerifyModalComponent } from '../components/verify-modal/verify-modal.component';
import { VerifyModalObject } from '../components/verify-modal/verify-modal.interface';
import { IDataSource, SourceType } from '../models/data-source';
import { ISubjectCodeTypes, ITokenizationInstructions, IVisitIdTypes, TokenizationInstructions } from '../models/tokenization-instructions';
import { IntegrationNavigationService } from './integration-navigation.service';
import { ProxyService } from './proxy.service';

@Injectable()
export class DataSourceService {
  private logger = new Logger(this);

  public dataSources: IDataSource[];

  private primarySourceList = [];
  private sourceList = [];
  public primaryToken = '';
  public sourcListsUpdate = new EventEmitter();
  public primaryDataSourceIsCSV = new BehaviorSubject<boolean>(false);
  public anEpicDataSourceExists = new BehaviorSubject<boolean>(false);

  private subjectCodeTypes: KeyValue<string, string>[] = [];

  public get SubjectCodeTypes(): KeyValue<string, string>[] {
    return this.subjectCodeTypes;
  }

  private visitIdTypes: KeyValue<string, string>[] = [];

  public get VisitIdTypes(): KeyValue<string, string>[] {
    return this.visitIdTypes;
  }

  constructor(
    private proxyService: ProxyService,
    private integrationNavigation: IntegrationNavigationService,
    private dialog: MatDialog) {
    this.dataSources = [];

    this.ResetSourceLists();
    // Create KeyValue subjectCodeTypes
    for (const key of Object.keys(ISubjectCodeTypes)) {
      this.subjectCodeTypes.push({key, value: key});
    }

    // Create KeyValue subjectCodeTypes
    for (const key of Object.keys(IVisitIdTypes)) {
      this.visitIdTypes.push({key, value: key});
    }

    // Remove CSV Option if this dataSource is primary and an epic data dataSource exists
    this.anEpicDataSourceExists.subscribe(result => {
        this.logger.debug('anEpicDataSourceExists$ initial', result, this.primarySourceList);
        this.logger.debug('anEpicDataSourceExists$ 1', result, this.primarySourceList);
        if (result) {
          this.removeColumnTypePrimary(SourceType.CSV);
        }
      });

    // Order Matters, we need to have the above observables set-up, before we call BuildColumnTypes()
    this.primaryDataSourceIsCSV.subscribe(value => {
      this.logger.debug('primaryDataSourceIsCSV$ initial', value);
      if (value) {
        this.logger.debug('primaryDataSourceIsCSV$ splicing', value);
        this.removeColumnTypePrimary(SourceType.EPIC);
        this.removeColumnTypeSecondary(SourceType.EPIC);
      }
    });
  }

  public get primaryList() {
    return this.primarySourceList;
  }

  public get secondaryList() {
    return this.sourceList;
  }

  public UpdateSourceLists() {
    this.ResetSourceLists();
    this.removeEpicFromPrimaryDataSourceLists();
    this.checkForEpicDataSource();
    this.checkForPrimaryDataSource();
    this.sourcListsUpdate.emit([this.primarySourceList, this.sourceList]);
  }

  private checkForPrimaryDataSource() {
    let result = false;
    let sourceType = null;
    let primarySourceCount = 0;
    this.dataSources.forEach(source => {
      if (source.primary) {
        sourceType = source.type;
        primarySourceCount++;
        if (source.type === SourceType.CSV) {
          result = true;
        }
      }
    });
    this.primaryDataSourceIsCSV.next(result);
    if (primarySourceCount > 1) {
      throw Error('Too many primary dataSources: ' + primarySourceCount);
    }
  }

  private checkForEpicDataSource() {
    let result = false;
    this.dataSources.forEach(source => {
      if (source.type === SourceType.EPIC) {
        result = true;
      }
    });
    this.anEpicDataSourceExists.next(result);
  }

  private removeEpicFromPrimaryDataSourceLists() {
    this.removeColumnTypePrimary(SourceType.EPIC);
  }

  public removeColumnTypePrimary(type: SourceType) {
    this.removeDataSourceTypeHelper(type, this.primarySourceList);
  }

  public removeColumnTypeSecondary(type: SourceType) {
    this.removeDataSourceTypeHelper(type, this.sourceList);
  }

  private removeDataSourceTypeHelper(type: SourceType, list: any[]) {
    const found = list.findIndex(column => column.value === type);
    if (found >= 0) {
      list.splice(found, 1);
    }
  }

  private ResetSourceLists() {
    this.primarySourceList = [];
    this.sourceList = [];
    for (const p of Object.keys(SourceType)) {
      if (p !== 'OTHER') {
        this.primarySourceList.push({label: p, value: SourceType[p]});
        this.sourceList.push({label: p, value: SourceType[p]});
      }
    }
  }

  public verifyModal(integrationID: string,
                     source_id: string,
                     saveEventStart: EventEmitter<any>,
                     saveEventCompleted: EventEmitter<any>,
                     important_tokens: any[],
                     important_tokens_order: any[],
                     processed_id: string,
                     isPrimarySource: boolean = false,
                     delimiter_positions: any[] = null,
                     eventError: EventEmitter<any> = new EventEmitter<any>()) {
    if (integrationID == null ||
      source_id == null) {
      this.logger.debug('Attempting to verifyModal improperly.', integrationID, source_id);
      return new EventEmitter();
    }

    const isDone = new EventEmitter();
    const config = new MatDialogConfig();
    const modalObject = new VerifyModalObject();
    modalObject.title = '';
    modalObject.message = 'In order to correctly join multiple datasets of varying formats, please';
    modalObject.message += 'help us divide the ID below into the proper format.';
    modalObject.tokens = [];
    modalObject.important_tokens = important_tokens;
    modalObject.delimiter_positions = delimiter_positions;
    modalObject.processed_id = processed_id;
    modalObject.matching_strategy = '';
    modalObject.isComplete = isDone;
    modalObject.config_id = integrationID;
    modalObject.source_id = source_id;
    modalObject.primaryToken = this.primaryToken;
    modalObject.isPrimarySource = isPrimarySource;
    modalObject.saveEventStart = saveEventStart;
    modalObject.saveEventCompleted = saveEventCompleted;
    modalObject.important_tokens_order = important_tokens_order;
    config.data = modalObject;
    config.width = '1000px';

    this.proxyService.getPreprocessedID(integrationID, '0').subscribe((value: any) => {
      this.logger.debug('getPreprocessedID - evaluateID', value);
      if (source_id === '0') {
        this.primaryToken = value.raw;
      }
      if (this.dialog.openDialogs.length === 0) {
        this.dialog.open(VerifyModalComponent, config);
      }
    });

    return isDone;
  }

  public setDefaultSubjectCodesIfAble(subjectCodes: ITokenizationInstructions[], primary: boolean) {
    if (subjectCodes.length <= 0) {
      if (primary) {
        subjectCodes.push(new TokenizationInstructions({
          type: ISubjectCodeTypes.ppid,
          columnLabel: 'Participant ID Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: true,
          configurableColumn: true
        }));
        subjectCodes.push(new TokenizationInstructions({
          type: ISubjectCodeTypes.mrn,
          columnLabel: 'Participant MRN Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: false,
          configurableColumn: false
        }));
      } else {
        subjectCodes.push(new TokenizationInstructions({
          type: null,
          columnLabel: 'Participant ID Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          configurableType: true,
          configurableColumn: true
        }));
      }
    }
  }

  public setDefaultVisitCodesIfAble(visitCodes: ITokenizationInstructions[], primary: boolean, sourceType: SourceType) {
    if (visitCodes.length <= 0) {
      if (primary) {
        const visitDateInstructions = new TokenizationInstructions({
          type: IVisitIdTypes.visitDate,
          columnLabel: 'Visit Date Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: false,
          configurableColumn: true
        });
        // set Default Date Format for open Specimen
        if (sourceType === SourceType.OPENSPECIMEN) {
          visitDateInstructions.format = 'MM-dd-yyyy HH:mm'; // Setting as default for open Specimen
        }
        visitCodes.push(visitDateInstructions);
        visitCodes.push(new TokenizationInstructions({
          type: IVisitIdTypes.visitCode,
          columnLabel: 'Visit Code Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: false,
          configurableColumn: false
        }));
        visitCodes.push(new TokenizationInstructions({
          type: IVisitIdTypes.visitLabel,
          columnLabel: 'Visit Label Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: false,
          configurableColumn: false
        }));
        visitCodes.push(new TokenizationInstructions({
          type: IVisitIdTypes.other,
          columnLabel: 'Other Visit Column',
          columnName: null,
          delimiter_positions: [],
          important_tokens: [],
          important_tokens_order: [],
          processed_id: null,
          configurableType: false,
          configurableColumn: false
        }));
      }
    }
  }

  public makePPIDConfigurable(subjectCodes: ITokenizationInstructions[]) {
    for (const tokenInstructions of subjectCodes) {
      if (tokenInstructions.type === ISubjectCodeTypes.ppid) {
        tokenInstructions.configurableColumn = true;
      }
    }
  }

  public extractColumnNames(instructions: ITokenizationInstructions[]) {
    const value = [];
    for (const instruction of instructions) {
      value.push({key: instruction.columnName, value: instruction.columnName});
    }
    return value;
  }
}

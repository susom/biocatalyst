import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BehaviorSubject, Observable } from 'rxjs';
import { Logger } from '../../classes/logger';
import { DataSource, IDataSource } from '../../models/data-source';
import { ISubjectCodeTypes, IVisitIdTypes } from '../../models/tokenization-instructions';
import { DataSourceService } from '../../services/data-source.services';
import { DataSourceOpenSpecimenPipes } from './data-source-open-specimen.pipes';

@Component({
  selector: 'app-data-source-openspecimen-form',
  templateUrl: './data-source-open-specimen.component.html',
  styleUrls: ['./data-source-open-specimen.component.scss'],
  providers: [DataSourceOpenSpecimenPipes]
})
export class DataSourceOpenSpecimenComponent implements OnInit, OnChanges {
  private logger = new Logger(this);
  @Input() source: DataSource = null;
  @Input() sources: IDataSource[] = [];
  @Input() sourceTypeChange = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();

  private cpid: BehaviorSubject<string> = new BehaviorSubject<string>('');

  public oSource = new BehaviorSubject<IDataSource>(null);

  constructor(private ref: ChangeDetectorRef,
              private dialog: MatDialog,
              private myDataSourceService: DataSourceService,
              private osPipes: DataSourceOpenSpecimenPipes) {
  }

  ngOnInit() {
    this.source.connection_details.URL = null;
    this.source.connection_details.credentials = null;
    this.cpid.next(this.source.connection_details.data);

    this.EnsureCorrectColumnNames();

    this.cpid.pipe(
      this.osPipes.ValidateAndSetResults(this),
      this.osPipes.TriggerChanges(this)
    ).subscribe();

    this.sourceTypeChange.pipe(
      this.osPipes.ResetTokens(this)
    ).subscribe();
  }

  private EnsureCorrectColumnNames() {
    for (let instructions of this.source.integration_details.subjectCode) {
      if (instructions.type === ISubjectCodeTypes.ppid) {
        instructions.columnName = 'PPID';
      }

      if (instructions.type === ISubjectCodeTypes.mrn) {
        instructions.columnName = 'MRN';
      }
    }

    for (const instructions of this.source.integration_details.visitId) {
      if (instructions.type === IVisitIdTypes.visitDate) {
        instructions.columnName = 'Specimen_Collection_Date';
      }

      if (instructions.type === IVisitIdTypes.visitLabel) {
        instructions.columnName = 'Visit# Event Label';
      }

      if (instructions.type === IVisitIdTypes.visitCode) {
        instructions.columnName = 'EVENT_NAME';
      }
    }
  }

  public get CPID(): BehaviorSubject<string> {
    return this.cpid;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.source) {
      if (this.source) {
        this.ProcessSource();
        this.Validation(this.source.connection_details.data);
        this.checkTokenProcessed();
        this.ref.detectChanges();
      }
    }
  }

  private ProcessSource() {
    this.oSource.next(this.source);
  }

  Validation(result: string) {
    this.source.valid = true;
    this.logger.debug('cpid', result);
    let regex: RegExp = new RegExp(/\D+/, 'gi');

    if (regex.test(result)) {
      this.source.valid = false;
    }
  }

  public get tokenInstruction() {
    // Assumption. We only have One PPID that is possible to connect to
    // this is aslo secondary data
    let tokenInstructions = this.source.integration_details.subjectCode.find(detail => {
      return detail.type === ISubjectCodeTypes.ppid;
    });
    return tokenInstructions;
  }

  public checkTokenProcessed() {
    // Checks if important_tokens are present, if so add .complete class to button,
    // otherwise show as default/incomplete
    if (this.tokenInstruction) {
      if (this.tokenInstruction.important_tokens.length > 0) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }
}




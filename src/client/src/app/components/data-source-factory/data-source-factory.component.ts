import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Logger } from '../../classes/logger';
import { DataSource, IDataSource } from '../../models/data-source';
import { IIntegration, Integration } from '../../models/integration';
import { DataSourceService } from '../../services/data-source.services';

@Component({
  selector: 'app-data-source-factory',
  templateUrl: './data-source-factory.component.html',
  styleUrls: ['./data-source-factory.component.scss'],
  animations: [
    trigger('showState', [
      state('shown', style({})),
      transition(':enter', [
        style({
          opacity: 0
        }),
        animate('0ms 125ms')
      ]),
      transition(':leave',
        animate(300, style({
          opacity: 0
        }))
      )
    ])
  ]
})
export class DataSourceFactoryComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  @Input() integration: IIntegration;
  @Input() dataSource: DataSource;
  @Output() dataSourceChange: EventEmitter<IDataSource>;
  @Output() dataSourceDelete: EventEmitter<IDataSource>;

  @Input() dataSources: IDataSource[];
  @Input() expanded: boolean;

  public onSourceTypeChange = new EventEmitter<any>();
  private validSource = false;
  private currentTitle;

  public COLUMN_TYPES: any[] = [];

  public myForm: FormGroup;
  public placeHolders: any = {};

  constructor(private dialog: MatDialog,
              private dataSourceService: DataSourceService) {
    this.integration = new Integration();
    this.dataSource = new DataSource();
    this.dataSources = [this.dataSource];
    this.expanded = false;
    this.dataSourceChange = new EventEmitter<IDataSource>();
    this.dataSourceDelete = new EventEmitter<IDataSource>();

    this.dataSourceService.sourcListsUpdate.subscribe(value => {
      if (this.dataSource) {
        this.COLUMN_TYPES = (this.dataSource.primary) ? value[0] : value[1];
      }
    });
  }

  ngOnInit() {
    this.validSource = this.dataSource.valid;
    this.currentTitle = this.dataSource.name;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.dataSource) {
      this.processDataSource();
    }
  }

  public onFormChange(): void {
    this.dataSourceChange.emit(this.dataSource);
  }

  private processDataSource() {
    if (this.dataSource) {
      const formControlObject = {};
      this.placeHolders = {};

      const type = 'type';
      const typeForm = this.createForm(formControlObject, type, this.dataSource);

      typeForm.valueChanges.subscribe(results => {
        this.dataSource.connection_details.data = undefined;
        this.dataSource.connection_details.filename = undefined;
        this.dataSource.type = results;
        this.dataSourceService.UpdateSourceLists();
        this.onSourceTypeChange.emit(this.dataSource.type);
      });

      this.COLUMN_TYPES = (this.dataSource.primary) ? this.dataSourceService.primaryList : this.dataSourceService.secondaryList;
      this.dataSourceService.UpdateSourceLists();
      this.myForm = new FormGroup(formControlObject);
    }
  }

  private createForm(formControlObject: any, propertyName: string, source: any, customPlaceholder: string = null): FormControl {
    const control = new FormControl(source[propertyName], {updateOn: 'blur'});
    control.setValidators(Validators.required);
    control.setValue(source[propertyName]);
    control.valueChanges.subscribe(value => {
      source[propertyName] = value;
      this.dataSourceChange.emit(this.dataSource);
    });
    control.markAsTouched();
    this.placeHolders[propertyName] = (customPlaceholder) ? customPlaceholder : propertyName;
    formControlObject[propertyName] = control;
    return control;
  }

  getControl(Control: string) {
    if (this.myForm) {
      return this.myForm.get(Control);
    }
  }

  getControlError(Control: string, Error: string) {
    if (this.myForm) {
      return this.getControl(Control).hasError(Error);
    }
  }
}

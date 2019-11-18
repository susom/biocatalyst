import { Component, EventEmitter, Inject, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import * as csv from 'csvtojson';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';
import { ColumnSelectorDropdown } from '../../models/column-selector-dropdown';
import { EventMappingData, IEventMappingData } from '../../models/event-mapping-table';
import { IInjectableEventMappingTableObject, InjectableEventMappingTableObject } from '../../models/injectable-event-mapping-table-object';

@Component({
  selector: 'app-event-mapping-table',
  templateUrl: './event-mapping-table.component.html',
  styleUrls: ['./event-mapping-table.component.scss']
})
export class EventMappingTableComponent implements OnInit, IInjectableEventMappingTableObject {
  constructor(
    @Inject(MAT_DIALOG_DATA) public injectable: InjectableEventMappingTableObject) {
    if (injectable) {
      this.source = injectable.source;
      this.destination = injectable.destination;
      this.connectingSource = injectable.connectingSource;
      this.sourceName = injectable.sourceName;
      this.mapping = injectable.mapping;
      this.headers = injectable.headers;
    }
  }

  public get TableHeaders$(): Observable<any[]> {
    return this.tableHeaders;
  }

  public get TableData$(): Observable<any[]> {
    return this.tableData;
  }

  private logger = new Logger(this);
  @Input() source: string = null;
  @Input() destination: string = null;
  @Input() connectingSource = 'From dataSource';
  @Input() sourceName = 'To destination';
  @Input() mapping: IEventMappingData = null;
  @Input() headers: KeyValue<string>[] = null;
  @Output() onChange: EventEmitter<IEventMappingData> = new EventEmitter<IEventMappingData>();

  private eventMappingData: EventMappingData = new EventMappingData();

  private triggerOnChange = new EventEmitter<any>();
  private onChangeChain = this.triggerOnChange.pipe(
    filter(data => !this.loading.getValue()),
    tap(value => {
      this.onChange.emit(this.eventMappingData);
    })
  );

  private tableHeaders: BehaviorSubject<KeyValue<string>[]> = new BehaviorSubject<KeyValue<string>[]>(null);
  private tableData: BehaviorSubject<any[]> = new BehaviorSubject<any[]>(null);
  private tableDataChain = this.tableData.pipe(
    tap(data => {
      const headers = this.ExtractHeadersFromData(data);
      this.tableHeaders.next(headers);
      this.eventMappingData.mapping = this.convertTableDataToMapping(
        data,
        this.sourceData.getValue(),
        this.destinationData.getValue()
      );
      this.triggerOnChange.emit();
    })
  );

  private sourceData: BehaviorSubject<string> = new BehaviorSubject<string>(null);
  private sourceDataChain = this.sourceData.pipe(
    tap(result => {
      this.eventMappingData.source = result;
      this.triggerOnChange.emit();
    })
  );

  private destinationData: BehaviorSubject<string> = new BehaviorSubject<string>(null);
  private destinationDataChain = this.destinationData.pipe(
    tap(result => {
      this.eventMappingData.destination = result;
      this.triggerOnChange.emit();
    })
  );

  private loading: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private loadingChain = this.loading.pipe(
    filter(loading => loading),
    tap(data => {
      this.Clear();
    })
  );

  private connectingSourceData: BehaviorSubject<string> = new BehaviorSubject<string>(null);
  private sourceNameData: BehaviorSubject<string> = new BehaviorSubject<string>(null);

  public integrationDetails: ColumnSelectorDropdown[] = [];

  private ExtractHeadersFromData(data): KeyValue<string>[] {
    const headers: KeyValue<string>[] = [];
    if (data) {
      if (data.length > 0) {
        const row = data[0];
        for (const p in row) {
          headers.push(new KeyValue<string>(p, p));
        }
      }
    }
    return headers;
  }

  ngOnChanges(changes: SimpleChanges) {
    let regenerateDetails = false;
    for (const propName in changes) {
      if (propName === 'connectingSource') {
        this.connectingSourceData.next(this.connectingSource);
        regenerateDetails = true;
      }
      if (propName === 'sourceName') {
        this.sourceNameData.next(this.sourceName);
        regenerateDetails = true;
      }
    }
    if (regenerateDetails) {
      this.GenerateDetails();
    }
  }

  public convertMappingToTableData(mappingData: IEventMappingData, source: string, destination: string): any[] {
    const tableFormat = [];
    if (!mappingData) {
      return tableFormat;
    }
    for (const key in mappingData) {
      const temp = {};
      temp[destination] = key;
      temp[source] = mappingData[key];
      tableFormat.push(temp);
    }
    return tableFormat;
  }

  public convertTableDataToMapping(tableData: any[], source: string, destination: string) {
    const mapping = {};
    if (tableData == null) {
      return mapping;
    }
    for (const row of tableData) {
      const mappingSource = row[destination];
      const mappingDestination = row[source];
      mapping[mappingSource] = mappingDestination;
    }
    return mapping;
  }

  ngOnInit() {
    this.sourceData.next(this.source);
    this.destinationData.next(this.destination);
    const convertedMappingData = this.convertMappingToTableData(this.mapping, this.source, this.destination);
    this.tableData.next(convertedMappingData);

    this.sourceNameData.next(this.sourceName);
    this.connectingSourceData.next(this.connectingSource);

    this.GenerateDetails();

    this.sourceDataChain.subscribe();
    this.destinationDataChain.subscribe();
    this.tableDataChain.subscribe();
    this.onChangeChain.subscribe();
  }

  private GenerateDetails() {
    this.integrationDetails = [];
    const generateDetails = (label: string, defaultValue: string = null, columns: KeyValue<string>[] = null, scssClass: string = 'ui-g-6', onSelection: (result: any) => any = (result: any) => {
      this.logger.debug(label + ' - Selection:', result);
    }) => {
      // Adds to the currentIntegration List
      this.integrationDetails.push(
        new ColumnSelectorDropdown({label, defaultValue, columns, scssClass, onSelection})
      );
    };

    const suffix = ' mapping ID\'s';

    generateDetails(this.sourceName.concat(suffix), this.destinationData.getValue(), null, null,
      (result: string) => {
        this.destinationData.next(result);
      }
    );

    generateDetails(this.connectingSource.concat(suffix), this.sourceData.getValue(), null, null,
      (result: string) => {
        this.sourceData.next(result);
      }
    );
  }

  public Clear() {
    this.tableHeaders.next(null);
    this.tableData.next(null);
    this.sourceData.next(null);
    this.destinationData.next(null);
  }

  public RemoveRow(index: number) {
    this.logger.debug('Removing Row', index);
    const temp = this.tableData.getValue();
    temp.splice(index, 1);
    this.tableData.next(temp);
  }

  public AddRow(amount: number = 1) {
    const data = this.tableData.getValue();
    const headers = this.tableHeaders.getValue();

    // Create Empty Row
    const tempRow = {};
    headers.forEach(value => {
      tempRow[value.key] = '';
    });

    for (let index = 0; index < amount; index++) {
      data.push(tempRow);
    }
    this.tableData.next(data);
  }

  Upload(event: any) {
    this.loading.next(true);
    const file = event.target.files[0];
    const reader = new FileReader();
    reader.onload = () => csv().fromString(reader.result as string).then((contents: any) => this.sourceData.next(contents));
    reader.readAsText(file);
  }
}

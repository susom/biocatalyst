import { EventEmitter } from '@angular/core';
import { KeyValue } from '../classes/system/pair';
import { EventMappingData, IEventMappingData } from './event-mapping-table';

export interface IInjectableEventMappingTableObject {
  source: string;
  destination: string;
  connectingSource: string;
  sourceName: string;
  mapping: IEventMappingData;
  headers: KeyValue<string>[];
  onChange: EventEmitter<IEventMappingData>;
}

export class InjectableEventMappingTableObject implements IInjectableEventMappingTableObject {
  public source: string;
  public destination: string;
  public connectingSource: string;
  public sourceName: string;
  public mapping: IEventMappingData;
  public headers: KeyValue<string>[];
  public onChange: EventEmitter<IEventMappingData>;

  constructor(source?: IInjectableEventMappingTableObject) {
    this.source = source ? source.source : '';
    this.destination = source ? source.destination : '';
    this.connectingSource = source ? source.connectingSource : 'From data dataSource';
    this.sourceName = source ? source.sourceName : 'To destination';
    this.mapping = source ? source.mapping : new EventMappingData();
    this.headers = source ? source.headers : [];
    this.onChange = new EventEmitter<IEventMappingData>();
  }
}

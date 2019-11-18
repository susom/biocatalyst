export interface IEventMappingData {
  source: string;
  destination: string;
  mapping: any;
}

export class EventMappingData implements IEventMappingData {
  public source: string;
  public destination: string;
  public mapping: any;

  constructor(source?: IEventMappingData) {
    this.source = source ? source.source : '';
    this.destination = source ? source.destination : '';
    this.mapping = source ? source.mapping : null;
  }
}

import { EventMappingData, IEventMappingData } from './event-mapping-table';
import { IVisitIdTypes } from './tokenization-instructions';

export interface IIntegrationConnectionVisitIdData {
  sourceId: string;
  type: IVisitIdTypes;
  dateMatchingRangeInDays: number;
  eventNameMapping: IEventMappingData;
}

export class IntegrationConnectionVisitIdData implements IIntegrationConnectionVisitIdData {
  public sourceId: string;
  public type: IVisitIdTypes;
  public dateMatchingRangeInDays: number;
  public eventNameMapping: IEventMappingData;

  constructor(source?: IIntegrationConnectionVisitIdData) {
    this.sourceId = source ? source.sourceId : '';
    this.type = source ? source.type : IVisitIdTypes.other;
    this.dateMatchingRangeInDays = source ? source.dateMatchingRangeInDays : 0;
    this.eventNameMapping = source ? source.eventNameMapping : new EventMappingData();
  }
}

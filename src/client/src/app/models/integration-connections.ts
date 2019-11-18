import { ISubjectCodeTypes, IVisitIdTypes } from './tokenization-instructions';

export interface IIntegrationConnectionVisitId {
  dateMatchingRangeInDays: number;
  eventNameMapping: any;
  type: IVisitIdTypes;
}

export class IntegrationConnectionVisitId implements IIntegrationConnectionVisitId {
  public dateMatchingRangeInDays: number;
  public eventNameMapping: any;
  public type: IVisitIdTypes;

  constructor(source?: IIntegrationConnectionVisitId) {
    this.dateMatchingRangeInDays = source ? source.dateMatchingRangeInDays : 0;
    this.eventNameMapping = source ? source.eventNameMapping : undefined;
    this.type = source ? source.type : undefined;
  }

  toJSON(): any {
    return {
      dateMatchingRangeInDays: this.dateMatchingRangeInDays,
      eventNameMapping: this.eventNameMapping,
      type: this.type
    };
  }
}

export interface IIntegrationConnection {
  sourceId: string;
  subjectCodeType: ISubjectCodeTypes;
  matching_strategy: string;
  visitId: IIntegrationConnectionVisitId;
}

export class IntegrationConnection implements IIntegrationConnection {
  public sourceId: string;
  public subjectCodeType: ISubjectCodeTypes;
  public matching_strategy: string;
  public visitId: IIntegrationConnectionVisitId;

  public constructor(source?: IIntegrationConnection) {
    this.sourceId = source ? source.sourceId : '';
    this.subjectCodeType = source ? source.subjectCodeType : ISubjectCodeTypes.ppid;
    this.matching_strategy = source ? source.matching_strategy : '';
    this.visitId = source ? new IntegrationConnectionVisitId(source.visitId) : new IntegrationConnectionVisitId();
  }

  toJSON(): any {
    return {
      sourceId: this.sourceId,
      subjectCodeType: this.subjectCodeType,
      matching_strategy: this.matching_strategy,
      visitId: this.visitId
    };
  }
}

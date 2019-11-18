import { EventEmitter } from '@angular/core';

export interface IVerifyModalObject {
  title: string;
  message: string;
  tokens: any[];
  important_tokens: any[];
  delimiter_positions: any[];
  processed_id: string;
  matching_strategy: string;
  isComplete: EventEmitter<any>;
  config_id: string;
  source_id: string;
  primaryToken: string;
  isPrimarySource: boolean;
  saveEventStart: EventEmitter<any>;
  saveEventCompleted: EventEmitter<any>;
  important_tokens_order: any[];
  growth_tokens: any[];
}

export class VerifyModalObject implements IVerifyModalObject {
  public title: string;
  public message: string;
  public tokens: any[];
  public important_tokens: any[];
  public delimiter_positions: any[];
  public processed_id: string;
  public matching_strategy: string;
  public isComplete: EventEmitter<any>;
  public config_id: string;
  public source_id: string;
  public primaryToken: string;
  public isPrimarySource: boolean;
  public saveEventStart: EventEmitter<any>;
  public saveEventCompleted: EventEmitter<any>;
  public important_tokens_order: any[];
  public growth_tokens: any[];

  constructor(source?: IVerifyModalObject) {
    this.title = source ? source.title : '';
    this.message = source ? source.message : '';
    this.tokens = source ? source.tokens : [];
    this.important_tokens = source ? source.important_tokens : [];
    this.delimiter_positions = source ? source.delimiter_positions : [];
    this.processed_id = source ? source.processed_id : '';
    this.matching_strategy = source ? source.matching_strategy : '';
    this.isComplete = source ? source.isComplete : new EventEmitter<any>();
    this.config_id = source ? source.config_id : '';
    this.source_id = source ? source.source_id : '';
    this.primaryToken = source ? source.primaryToken : '';
    this.isPrimarySource = source ? source.isPrimarySource : false;
    this.saveEventStart = source ? source.saveEventStart : new EventEmitter<any>();
    this.saveEventCompleted = source ? source.saveEventCompleted : new EventEmitter<any>();
    this.important_tokens_order = source ? source.important_tokens_order : [];
    this.growth_tokens = source ? source.growth_tokens : [];
  }
}

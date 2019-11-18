export interface IFilterRange {
  from: any;
  to: any;
}

export interface IFilterValue {
  from: any;
  to: any;
}

export interface IFilter {
  field: string;
  operator: string;
  value: IFilterRange | IFilterValue;
}

export class Filter implements IFilter {
  public field: string;
  public operator: string;
  public value: IFilterRange | IFilterValue;

  constructor(source?: IFilter) {
    this.field = source ? source.field : '';
    this.operator = source ? source.operator : '';
    this.value = source ? source.value : { from: null, to: null };
  }
}

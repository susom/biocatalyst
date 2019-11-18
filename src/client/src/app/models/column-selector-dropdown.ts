import { KeyValue } from '@angular/common';

export interface IColumnSelectorDropdown {
  label: string;
  defaultValue: string;
  columns?: KeyValue<string, string>[];
  scssClass?: string;
  onSelection?: (result: any) => any;
}

export class ColumnSelectorDropdown implements IColumnSelectorDropdown {
  public label: string;
  public defaultValue: string;
  public columns: KeyValue<string, string>[];
  public scssClass: string;
  public onSelection: (result: any) => any;

  constructor(source?: IColumnSelectorDropdown) {
    this.label = source ? source.label : '';
    this.defaultValue = source ? source.defaultValue : '';
    this.columns = source ? source.columns : [];
    this.scssClass = source ? source.scssClass : '';
    this.onSelection = source ? source.onSelection : (() => {});
  }
}

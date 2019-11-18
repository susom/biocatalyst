import { ColumnSelectorDropdown } from './column-selector-dropdown';

export interface IDropdownLayout {
  details: ColumnSelectorDropdown;
  type?: ColumnSelectorDropdown;
  configureColumn?: boolean;
  scssSpacing?: string;
  disableColumnConfiguration?: boolean;
  hideColumnConfiguration?: boolean;
  showAlternateColumnConfiguration?: boolean;
  removeable?: boolean;
  data?: any;
  onRemove?: (result: any) => any;
}

export class DropdownLayout implements IDropdownLayout {
  public details: ColumnSelectorDropdown;
  public type: ColumnSelectorDropdown;
  public configureColumn: boolean;
  public scssSpacing: string;
  public disableColumnConfiguration: boolean;
  public hideColumnConfiguration: boolean;
  public showAlternateColumnConfiguration: boolean;
  public removeable: boolean;
  public data: any;
  public onRemove: (result: any) => any;

  constructor(source?: IDropdownLayout) {
    this.details = source ? source.details : new ColumnSelectorDropdown();
    this.type = source ? source.type : new ColumnSelectorDropdown();
    this.configureColumn = source ? source.configureColumn : false;
    this.scssSpacing = source ? source.scssSpacing : 'ui-g-12';
    this.disableColumnConfiguration = source ? source.disableColumnConfiguration : false;
    this.hideColumnConfiguration = source ? source.hideColumnConfiguration : false;
    this.showAlternateColumnConfiguration = source ? source.showAlternateColumnConfiguration : false;
    this.removeable = source ? source.removeable : false;
    this.data = source ? source.data : null;
    this.onRemove = source ? source.onRemove : null;
  }

  public setSpacing(units: number) {
    const spacing = 12 / units;
    if (spacing % 1 === 0) {
      this.scssSpacing = 'ui-g-' + spacing;
    }
  }
}

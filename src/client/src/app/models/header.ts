export interface IHeader {
  Label: string;
  Hidden: boolean;
  Filter: string;
  SortOrder: number;
  Order: number;
  Type: any;
}

export class Header implements IHeader {
  public Label: string;
  public Hidden: boolean;
  public Filter: string;
  public SortOrder: number;
  public Order: any;
  public Type: any;

  public constructor(source?: IHeader) {
    this.Label = source ? source.Label : '';
    this.Hidden = source ? source.Hidden : false;
    this.Filter = source ? source.Filter : '';
    this.SortOrder = source ? source.SortOrder : 0;
    this.Order = source ? source.Order : -1;
    this.Type = source ? source.Type : 0;
  }

  public toJSON(): any {
    return {
      Label: this.Label,
      Hidden: this.Hidden,
      Filter: this.Filter,
      SortOrder: this.SortOrder,
      Order: this.Order,
      Type: this.Type
    };
  }
}

export enum ISubjectCodeTypes {
  mrn = 'mrn',
  ppid = 'ppid'
}

export enum IVisitIdTypes {
  visitDate = 'visitDate',
  visitLabel = 'visitLabel',
  visitCode = 'visitCode',
  other = 'other'
}

export interface ITokenizationInstructions {
  type: ISubjectCodeTypes | IVisitIdTypes;
  columnLabel: string;
  columnName: string;
  delimiter_positions: string[];
  important_tokens: string[];
  important_tokens_order: string[];
  processed_id?: string;
  configurableType?: boolean;
  configurableColumn?: boolean;
  removeable?: boolean;
  growth_tokens?: string[];
  format?: string;
  format_moment?: string;
}

export class TokenizationInstructions implements ITokenizationInstructions {
  public type: ISubjectCodeTypes | IVisitIdTypes;
  public columnLabel: string;
  public columnName: string;
  public delimiter_positions: string[];
  public important_tokens: string[];
  public important_tokens_order: string[];
  public processed_id: string;
  public configurableType: boolean;
  public configurableColumn: boolean;
  public removeable: boolean;
  public growth_tokens: string[];
  public format: string;
  public format_moment: string;

  public constructor(source?: ITokenizationInstructions) {
    this.type = source ? source.type : null;
    this.columnLabel = source ? source.columnLabel : '';
    this.columnName = source ? source.columnName : '';
    this.delimiter_positions = source ? source.delimiter_positions : [];
    this.important_tokens = source ? source.important_tokens : [];
    this.important_tokens_order = source ? source.important_tokens_order : [];
    this.processed_id = source ? source.processed_id : '';
    this.configurableType = source ? source.configurableType : true;
    this.configurableColumn = source ? source.configurableColumn : true;
    this.removeable = source ? source.removeable : false;
    this.growth_tokens = source ? source.growth_tokens : [];
    this.format = source ? source.format : '';
    this.format_moment = source ? source.format_moment : '';
  }

  toJSON(): any {
    return {
      type: this.type,
      columnLabel: this.columnLabel,
      columnName: this.columnName,
      delimiter_positions: this.delimiter_positions,
      important_tokens: this.important_tokens,
      important_tokens_order: this.important_tokens_order,
      processed_id: this.processed_id,
      configurableType: this.configurableType,
      configurableColumn: this.configurableColumn,
      removeable: this.removeable,
      growth_tokens: this.growth_tokens,
      format: this.format,
      format_moment: this.format_moment
    };
  }
}

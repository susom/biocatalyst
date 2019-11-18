import { Header } from './header';

export enum ReportState {
  'Show',
  'Hidden',
  'Delete'
}

export interface IReport {
  Name: string;
  _ids: string;
  Filters: Header[];
  VisableColumns: Header[];
  State: ReportState;
  IntegrationID: string;
  Query: string;
}

export class Report implements IReport {
  public Name: string;
  public _ids: string;
  public Filters: Header[];
  public VisableColumns: Header[];
  public State: ReportState;
  public IntegrationID: string;
  public Query: string;

  constructor(source?: IReport) {
    this.Name = 'New Report';
    this._ids = null;
    this.Filters = [];
    this.VisableColumns = [];
    this.State = ReportState.Show;
    this.IntegrationID = '';
    this.Query = '';

    if (source) {
      Object.keys(source).forEach((property) => {
        if (this[property]) {
          this[property] = source[property];
        }
      });
    }
  }

  public toJSON(): any {
    return {
      Name: this.Name,
      _ids: this._ids,
      Filters: this.Filters,
      VisableColumns: this.VisableColumns,
      State: this.State,
      IntegrationID: this.IntegrationID,
      Query: this.Query
    };
  }
}

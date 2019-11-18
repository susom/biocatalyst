import { Logger } from '../classes/logger';
import { DataSource, IDataSource } from './data-source';

export interface IIntegration {
  _ids: string;
  name: string;
  dataSources: IDataSource[];
  valid?: boolean;
}

export class Integration implements IIntegration {
  private logger = new Logger(this);

  public _ids: string;
  public name: string;
  public dataSources: IDataSource[];

  private isValid: boolean;

  public get valid(): boolean {
    return this.isValid || (this.dataSources.length <= 0 && this.dataSources.every(source => source.valid));
  }

  public set valid(isValid: boolean) {
    this.isValid = isValid;
  }

  public static getNextSourceId(dataSources: IDataSource[]) {
    let id = 0;
    while (!dataSources.every(source => source.source_id !== id.toString())) {
      id++;
    }
    return id;
  }

  constructor(source?: IIntegration) {
    this.name = source ? source.name : 'New Integration Configuration';
    this._ids = source ? source._ids : '';
    this.dataSources = source ? Array.from(source.dataSources, (x) => new DataSource(x)) : [];
    this.isValid = source ? source.valid : false;
  }

  public avoidNameCollision(name: string): string {
    const count = this.dataSources.reduce((accumulator, currentSource) => {
      if (currentSource.name === name) {
        return accumulator + 1;
      }
    }, 0);
    return count > 0 ? `${name} (${count})` : name;
  }

  toJSON(): any {
    return {
      name: this.name,
      _ids: this._ids,
      dataSources: this.dataSources,
      valid: this.valid
    };
  }
}

import { BehaviorSubject, Observable } from 'rxjs';

export enum MRNSourceType {
  'NONE',
  'FILE',
  'TEXT',
  'DATA_SOURCE'
}

export class FileSource {
  constructor(
    public filename = '',
    public mrns: string[] = []
  ) {
  }

  toJSON(): any {
    return {
      'mrns': this.mrns,
      'filename': this.filename
    };
  }
}

export class TextSource {
  constructor(public mrns: string[] = []) {
  }

  toJSON(): any {
    return {
      'mrns': this.mrns
    };
  }
}

export class DataSource {
  constructor(
    public dataSourceID = '',
    public dataSourceColumns = '') {
  }

  toJSON(): any {
    return {
      'dataSourceID': this.dataSourceID,
      'dataSourceColumns': this.dataSourceColumns
    };
  }

}

export interface IMRNSource {
  type: MRNSourceType;
  source: FileSource | TextSource | DataSource;
}

export class MrnSource implements IMRNSource {
  _type = new BehaviorSubject<MRNSourceType>(MRNSourceType.NONE);

  constructor(public source: FileSource | TextSource | DataSource) {
    this._type.subscribe(value => {
      switch (value) {
        case MRNSourceType.FILE:
          this.source = new FileSource();
          break;
        case MRNSourceType.TEXT:
          this.source = new TextSource();
          break;
        case MRNSourceType.DATA_SOURCE:
          this.source = new DataSource();
          break;
        default:
          this.source = undefined;
          break;
      }

    });
  }

  public set type(SourceType: MRNSourceType) {
    this._type.next(SourceType);
  }

  public get type(): MRNSourceType {
    return this._type.getValue();
  }

  public get type$(): Observable<MRNSourceType> {
    return this._type;
  }
}

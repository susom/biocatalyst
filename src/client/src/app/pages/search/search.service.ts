import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ISearchEntry } from '../../components/search-entry/search-entry';

@Injectable()
export class SearchService {

  private _query = new BehaviorSubject<string>('');
  private _searchEntry = new BehaviorSubject<ISearchEntry>(null);

  public set query(value: string) {
    this._query.next(value);
  }

  public get query() {
    return this._query.getValue();
  }

  public get query$() {
    return this._query;
  }

  public set searchEntry(value: ISearchEntry) {
    this._searchEntry.next(value);
  }

  public get searchEntry() {
    return this._searchEntry.getValue();
  }

  public get searchEntry$() {
    return this._searchEntry;
  }

  constructor() {
  }

}

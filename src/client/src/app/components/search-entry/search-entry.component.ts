import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ISearchEntry, SearchEntry } from './search-entry';

@Component({
  selector: 'search-entry',
  templateUrl: './search-entry.component.html',
  styleUrls: ['./search-entry.component.scss']
})
export class SearchEntryComponent implements OnInit {

  @Input() value: ISearchEntry = new SearchEntry('Title', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.', null);
  @Output() onClick = new EventEmitter();

  ngOnInit() {
  }

  public get title() {
    return this.value.title;
  }

  public get description() {
    return this.value.description;
  }
}

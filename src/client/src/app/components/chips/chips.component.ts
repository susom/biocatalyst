import { Component, OnInit } from '@angular/core';
import * as moment from 'moment';
import { NumericRange } from '../../classes/elastic-model';
import { Logger } from '../../classes/logger';
import { Header } from '../../models/header';
import { IReport, Report } from '../../models/report';
import { ElasticFieldDataTypes } from '../../services/proxy.service';
import { ReportNavigationService } from '../../services/report-navigation.service';
import { ReportStoreService } from '../../services/report-store.service';
import { SearchDataService } from '../../services/search-data.service';

@Component({
  selector: 'app-chips',
  templateUrl: './chips.component.html',
  styleUrls: ['./chips.component.scss']
})
export class ChipsComponent implements OnInit {
  private logger = new Logger(this);
  private headers: Header[] = [];
  private searchState: IReport;

  constructor(
    private reportNav: ReportNavigationService,
    private reportStore: ReportStoreService,
    private searchData: SearchDataService) {
    this.searchState = new Report();
  }

  ngOnInit() {
  }

  public get Headers(): Header[] {
    return this.headers;
  }

  public Filter(header: Header): string {
    const result = header.Filter as any;

    if (header.Type === ElasticFieldDataTypes.Date) {

      const date = result;
      const start = (date.start) ? moment(date.start).utc().format('dddd, MMMM Do YYYY') : null;
      const end = (date.end) ? moment(date.end).utc().format('dddd, MMMM Do YYYY') : null;

      if (start != null) {
        if (end == null) {
          const greaterThan = 'Greater than ';
          return greaterThan
            .concat(
              start.toString()
            );

        }
      }

      // end exits: start Doesn't ( Less Than)
      if (end != null) {
        if (start == null) {
          const lessThan = 'Less than ';
          return lessThan
            .concat(
              end.toString()
            );

        }
      }

      // start exits: end exists ( range )
      if (end != null) {
        if (start != null) {
          return start.toString()
            .concat('-')
            .concat(
              end.toString()
            );
        }
      }
      return null;
    }
    if (header.Type === ElasticFieldDataTypes.Numeric) {
      const value = result as NumericRange;
      const start = value.gte;
      const end = value.lt;

      // Need to use Null explicitly 0 is considered null under 'if(value)'
      // start exits: end Doesn't ( Greater Than)
      // this.logger.debug("ChipsComponent", header, start, end);
      if (start != null) {
        if (end == null) {
          const greaterThan = 'Greater than ';
          return greaterThan
            .concat(
              start.toString()
            );

        }
      }

      // end exits: start Doesn't ( Less Than)
      if (end != null) {
        if (start == null) {
          const lessThan = 'Less than ';
          return lessThan
            .concat(
              end.toString()
            );

        }
      }

      // start exits: end exists ( range )
      if (end != null) {
        if (start != null) {
          return start.toString()
            .concat('-')
            .concat(
              end.toString()
            );
        }
      }
    }

    if (!result) {
      return null;
    }

    return result;
  }

  public RemoveSource(header: Header) {
    const Label = header.Label;
    const regex: RegExp = new RegExp(/^j\d+\_s\d\d?_/);
    const match = regex.exec(Label);
    if (match) {
      // There should be only one match and it's stored in the first index;
      const mat = match[0];
      const source = Label.replace(mat, '').trim();
      this.logger.debug('Remove Source', match);
      return source;
    }
    return Label;
  }

  public ClearHeader(header: Header) {
    header.Filter = '';
    this.searchState.Filters = this.headers;
    this.searchData.refreshData();
  }

}

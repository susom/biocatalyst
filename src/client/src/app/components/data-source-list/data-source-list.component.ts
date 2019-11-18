import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Logger } from '../../classes/logger';
import { DataSource, IDataSource } from '../../models/data-source';
import { IIntegration, Integration } from '../../models/integration';
import { DataSourceService } from '../../services/data-source.services';

@Component({
  selector: 'app-data-source-list',
  templateUrl: './data-source-list.component.html',
  styleUrls: ['./data-source-list.component.scss'],
  animations: [
    trigger('showState', [
      state('shown', style({})),
      transition(':enter', [
        style({
          opacity: 0
        }),
        animate('100ms 500ms')
      ]),
      transition(':leave',
        animate(300, style({
          opacity: 0
        }))
      )
    ])
  ],
  providers: [DataSourceService]
})
export class DataSourceListComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  @Input() integration: IIntegration;
  @Input() dataSources: IDataSource[];
  @Output() dataSourcesChange: EventEmitter<IDataSource[]>;

  constructor() {
    this.integration = new Integration();
    this.dataSources = [];
    this.dataSourcesChange = new EventEmitter<IDataSource[]>();
  }

  public ngOnInit(): void {
  }

  public ngOnChanges(changes: SimpleChanges): void {
  }

  public delete(index: number) {
    this.dataSources.splice(index, 1);
    this.dataSourcesChange.emit(this.dataSources);
  }

  public hasSingleSource() {
    return this.dataSources.length === 1;
  }

  public addDataSource() {
    const newDataSource = new DataSource();
    newDataSource.source_id = Integration.getNextSourceId(this.dataSources).toString();
    this.dataSources.push(newDataSource);
    this.dataSourcesChange.emit(this.dataSources);
  }
}

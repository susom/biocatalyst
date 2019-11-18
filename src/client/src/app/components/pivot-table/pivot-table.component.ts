import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { DxChartComponent, DxPivotGridComponent } from 'devextreme-angular';
import PivotGridDataSource from 'devextreme/ui/pivot_grid/data_source';
import { Logger } from '../../classes/logger';

@Component({
  selector: 'app-pivot-table',
  templateUrl: './pivot-table.component.html',
  styleUrls: ['./pivot-table.component.scss']
})
export class PivotDataCollectionComponent implements OnInit, AfterViewInit, OnChanges {
  private logger = new Logger(this);

  @ViewChild(DxPivotGridComponent, null) pivotGrid: DxPivotGridComponent;
  @ViewChild(DxChartComponent, null) chart: DxChartComponent;

  @Input() data: any[] = [];
  @Input() usePreviousHeadersOnUpdate = false;
  @Input() refreshTable = new EventEmitter();
  @Input() idField = null;
  @Input() isLoading: boolean;
  @Input() selectedData: any = [];

  pivotGridDataSource: any;
  public state: any;
  exportVisible: boolean;
  printVisible: boolean;

  constructor(private ref: ChangeDetectorRef) {
    if (this.pivotGridDataSource) {
      this.state = this.pivotGridDataSource.state();
    }
  }

  ngOnInit() {
    this.CreateDataSource(this.data,
      [{
        dataField: 's0_specimen_type',
        dataType: 'string',
        area: 'column',
        caption: 'Specimen Type'
      },
        {
          caption: 'Count',
          dataField: '@biocatalyst-id',
          dataType: 'string',
          area: 'data'
        }]
    );

    this.refreshTable.subscribe(value => {
      this.CreateDataSource(this.data, this.pivotGridDataSource.fields());
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    for (let propName in changes) {
      if (propName == 'data') {
        if (this.pivotGridDataSource) {
          // Use the previous fields when updating the
          // the grid data so that we maintain the same information
          // integration already. Otherwise we will loose our current field
          // selections as devextreme will want to update it to the new
          // data.
          this.CreateDataSource(this.data, this.pivotGridDataSource.fields());
        }
      }
    }
  }

  ngAfterViewInit() {
    this.pivotGrid.instance.bindChart(this.chart.instance, {
      dataFieldsDisplayMode: 'splitPanes',
      alternateDataFields: false
    });
  }

  toggleExport() {
    this.exportVisible = !this.exportVisible;
  }

  togglePrint() {
    this.printVisible = !this.printVisible;
  }

  print(chartType) {
    if (chartType === 'chart') {
      this.printVisible = !this.printVisible;
      this.chart.instance.print();
    } else if (chartType === 'both') {
      this.printVisible = !this.printVisible;
      window.print();
    }
  }

  private CreateDataSource(data: any, fields: any = null) {
    let options = {
      store: this.data
    };
    if (this.usePreviousHeadersOnUpdate) {
      if (fields) {
        options['fields'] = fields;
      }
    }
    this.pivotGridDataSource = new PivotGridDataSource(options);
    this.logger.debug('CreateDataSource', options);
  }
}

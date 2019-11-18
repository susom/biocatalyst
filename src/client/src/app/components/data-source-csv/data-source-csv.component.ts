import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Logger } from '../../classes/logger';
import { DataSource, IDataSource } from '../../models/data-source';
import { IIntegration, Integration } from '../../models/integration';
import { ISubjectCodeTypes } from '../../models/tokenization-instructions';
import { FileInformation } from '../../models/file-information';
import { ProxyService } from '../../services/proxy.service';
import { DataSourceService } from '../../services/data-source.services';

@Component({
  selector: 'app-data-source-csv-form',
  templateUrl: './data-source-csv.component.html',
  styleUrls: ['./data-source-csv.component.scss']
})
export class DataSourceCsvComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  @Input() integration: IIntegration;

  @Input() dataSource: DataSource;
  @Output() dataSourceChange = new EventEmitter<DataSource>();

  @Input() sourceType: string;
  @Output() sourceTypeChange: EventEmitter<string>;

  @Input() dataSources: IDataSource[];

  public options: string[];
  public preview: any;

  private hasLocalStorageSpace: boolean;
  private fileInformation: FileInformation;

  constructor(private dataSourceService: DataSourceService, private proxyService: ProxyService) {
    this.integration = new Integration();

    this.hasLocalStorageSpace = false;
    this.fileInformation = {fileData: [], fileName: '', fileHeaders: []};

    this.dataSourceChange = new EventEmitter<DataSource>();
    this.dataSources = [];

    this.sourceType = '';
    this.sourceTypeChange = new EventEmitter<string>();

    this.options = [];
    this.preview = {};
  }

  ngOnInit() {
    this.dataSourceService.makePPIDConfigurable(this.dataSource.integration_details.subjectCode);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dataSource) {
      if (this.dataSource.connection_details.data == null) {
        this.getLocalStorageSpace();
      } else {
        this.hasLocalStorageSpace = true;
        this.getPreview();
      }

      const tokenInstructions = this.dataSource.integration_details.subjectCode.find(detail => {
        return detail.type === ISubjectCodeTypes.ppid;
      });

      if (tokenInstructions) {
        tokenInstructions.delimiter_positions = [];
        tokenInstructions.growth_tokens = [];
        tokenInstructions.important_tokens = [];
        tokenInstructions.important_tokens_order = [];
        tokenInstructions.processed_id = null;
      }
    }
  }

  public getLocalStorageSpace() {
    if (!this.hasLocalStorageSpace) {
      this.hasLocalStorageSpace = true;
      const storage = {type: 'csv', data: {rows: []}};
      this.proxyService.createItem(JSON.stringify(storage)).subscribe((result) => {
        this.dataSource.connection_details.data = result._id;
      });
    }
  }

  public async getPreview(): Promise<any> {
    return this.proxyService.previewItem(this.dataSource.connection_details.data).then((result) => {
      const data = result.data.rows;
      const headers = data[0] ? Object.keys(data[0]) : [];
      this.preview = {data, headers};
      this.options = headers;
    });
  }

  public onFileUploaded(info: FileInformation) {
    if (info) {
      const storageObject = {type: 'csv', data: {rows: info.fileData}};
      this.proxyService.updateItem(this.dataSource.connection_details.data, JSON.stringify(storageObject)).subscribe((result) => {
        this.logger.debug(`Connection updated for CSV file source`);
        this.options = info.fileHeaders;
      });
    }
  }

  check($event: any) {
    this.logger.debug($event);
  }
}

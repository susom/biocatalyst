import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Logger } from '../../classes/logger';
import { FileInformation } from '../../models/file-information';

@Component({
  selector: 'app-csv-upload-table',
  templateUrl: './csv-upload-table.component.html',
  styleUrls: ['./csv-upload-table.component.scss']
})
export class CsvUploadTableComponent implements OnInit {
  private logger = new Logger(this);

  @Input() fileData: any[];
  @Input() fileHeaders: string[];
  @Output() fileUploaded: EventEmitter<FileInformation>;

  public fileName: string;

  constructor() {
    this.fileName = '';
    this.fileData = [];
    this.fileHeaders = [];
    this.fileUploaded = new EventEmitter<FileInformation>();
  }

  ngOnInit() {
  }

  public onFileUploaded(event: [string, any]) {
    this.fileName = event[0];
    this.fileData = event[1];
    this.fileHeaders = Object.keys(this.fileData[0]);
    this.fileUploaded.emit({
      fileData: this.fileData,
      fileName: this.fileName,
      fileHeaders: this.fileHeaders
    });
  }
}

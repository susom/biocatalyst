import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import * as moment from 'moment';
import { Logger } from '../../classes/logger';
import { Overwrite } from './overwrite';

@Component({
  selector: 'app-save-report-dialog',
  templateUrl: './save-report-dialog.component.html',
  styleUrls: ['./save-report-dialog.component.scss']
})
export class SaveReportDialogComponent implements OnInit {
  private logger = new Logger(this);

  public text: string;
  public overWrite: Overwrite;

  @Output() onSave: EventEmitter<string>;
  @Output() onClose: EventEmitter<string>;
  @Output() checkForOverwrite: EventEmitter<string>;

  constructor() {
    this.text = '';
    this.overWrite = new Overwrite();

    this.onSave = new EventEmitter<string>();
    this.onClose = new EventEmitter<string>();
    this.checkForOverwrite = new EventEmitter<string>();
  }

  ngOnInit() {
    this.text = moment().format('dddd, MMMM Do YYYY');

    this.overWrite.cancel = () => {
      this.overWrite.reset();
    };

    this.overWrite.confirm = () => {
      this.onSave.emit();
      this.onClose.emit();
    };

    this.onClose.subscribe(event => {
      this.text = '';
      this.overWrite.reset();
    });
  }
}

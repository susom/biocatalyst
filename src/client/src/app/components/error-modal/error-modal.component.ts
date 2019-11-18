import { Component, Inject, Input } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Logger } from '../../classes/logger';

@Component({
  selector: 'app-error-modal',
  templateUrl: './error-modal.component.html',
  styleUrls: ['./error-modal.component.scss']
})
export class ErrorModalComponent {
  private logger = new Logger(this);

  @Input() title = 'OOPS!';
  @Input() message = '';

  constructor(
    @Inject(MAT_DIALOG_DATA) public dataObject: any,
    public dialogRef: MatDialogRef<ErrorModalComponent>) {
    if (dataObject) {
      if (dataObject.title) {
        this.title = dataObject.title;
      }
      if (dataObject.message) {
        this.message = dataObject.message;
      }
    }
  }

  public Close() {
    this.dialogRef.close();
  }
}

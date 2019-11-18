import { ComponentPortal, Portal } from '@angular/cdk/portal';
import { Component, Inject, OnInit, SimpleChanges } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Logger } from '../../../classes/logger';
import { ISimpleDialogObject } from '../simple-dialog-object';

@Component({
  selector: 'simple-dialog-portal',
  templateUrl: './simple-dialog-portal.component.html',
  styleUrls: ['./simple-dialog-portal.component.scss']
})
export class SimpleDialogPortalComponent implements OnInit {
  private logger: Logger = new Logger(this);
  selectedPortal: Portal<any>;
  dialogObject: ISimpleDialogObject<any> = null;

  // Soon to be named - SimpleDialogePortal
  constructor(
    @Inject(MAT_DIALOG_DATA) public dataObject: ISimpleDialogObject<any>,
    public dialogRef: MatDialogRef<any>) {
    if (dataObject) {
      this.dialogObject = dataObject;
      if (this.dialogObject.component) {
        this.selectedPortal = new ComponentPortal(this.dialogObject.component);
      }
    }
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    this.logger.debug('ngOnChanges', changes);
  }

  ngOnDestroy() {

  }

  Close() {
    if (this.dialogRef) {
      this.dialogRef.close();
    }
  }

}

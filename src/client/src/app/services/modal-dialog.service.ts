import { EventEmitter, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogConfig } from '../dialogs/dialog-config';
import { SimpleDialogPortalComponent } from '../dialogs/simple-dialog/simple-dialog-portal/simple-dialog-portal.component';

@Injectable()
export class ModalDialogService {
  public static readonly Simple = SimpleDialogPortalComponent;
  private dialogRef: MatDialogRef<any> = null;

  private dialogOpened: EventEmitter<any>;
  private dialogClosed: EventEmitter<any>;

  constructor(private dialog: MatDialog) {
    this.dialogOpened = new EventEmitter<any>();
    this.dialogClosed = new EventEmitter<any>();
  }

  public open(config: DialogConfig) {
    if (this.dialogRef != null) {
      this.dialogRef.close();
    } else {
      this.dialogRef = this.dialog.open(ModalDialogService.Simple, config);
      this.dialogRef.afterClosed().subscribe(() => this.close());
      this.dialogOpened.emit();
    }
  }

  public close() {
    if (this.dialogRef) {
      this.dialogRef.close();
    }
    this.dialogRef = null;
    this.dialogClosed.emit();
  }
}

import { EventEmitter, Type } from '@angular/core';

export interface ISimpleDialogObject<T> {
  component: Type<T>;
  onClose: EventEmitter<any>;
}

export class SimpleDialogObject<T> implements ISimpleDialogObject<T> {

  constructor(
    public component: Type<T> = null,
    public onClose: EventEmitter<any> = null
  ) {
  }

  public static Copy<T>(A: ISimpleDialogObject<T>, B: ISimpleDialogObject<T>) {
    // Copies A into B
    for (let property in A) {
      B[property] = A[property];
    }
  }

  public static Close(dialog: ISimpleDialogObject<any>, value: any = null) {
    if (dialog) {
      if (dialog.onClose) {
        dialog.onClose.emit(null);
      }
    }
  }

}

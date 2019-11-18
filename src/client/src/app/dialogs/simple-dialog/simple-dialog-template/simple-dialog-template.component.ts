import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-simple-dialog-template',
  templateUrl: './simple-dialog-template.component.html',
  styleUrls: ['./simple-dialog-template.component.scss']
})
export class SimpleDialogTemplateComponent {
  @Output() close = new EventEmitter();

  Close() {
    this.close.emit();
  }
}

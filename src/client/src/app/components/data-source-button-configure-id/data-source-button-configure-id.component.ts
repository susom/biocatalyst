import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-data-source-button-configure-id',
  templateUrl: './data-source-button-configure-id.component.html',
  styleUrls: ['./data-source-button-configure-id.component.scss']
})
export class DataSourceButtonConfigureIDComponent {
  @Input() formatIDRequired: boolean;
  @Input() disabled: boolean;
  @Output() buttonPressed: EventEmitter<any>;

  constructor() {
    this.formatIDRequired = true;
    this.disabled = false;
    this.buttonPressed = new EventEmitter<any>();
  }
}

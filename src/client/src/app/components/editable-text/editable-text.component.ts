import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-editable-text',
  templateUrl: './editable-text.component.html',
  styleUrls: ['./editable-text.component.scss']
})
export class EditableTextComponent implements OnChanges {
  @Input() editable: boolean;
  @Input() editing: boolean;

  @Input() text: string;
  @Output() textChange: EventEmitter<string>;

  @Output() done: EventEmitter<void>;

  constructor() {
    this.text = '';
    this.textChange = new EventEmitter<string>();

    this.editable = true;
    this.editing = false;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.editable) {
      this.editing = false;
    }
  }

  public stopEditing() {
    this.editing = false;
    this.textChange.emit(this.text);
  }

  public startEditing() {
    this.editing = true;
  }

  public onClick() {
    if (this.editable) {
      this.startEditing();
    }
  }

  public inputKeyup(keyEvent: KeyboardEvent) {
    if (keyEvent.key === 'Enter') {
      this.stopEditing();
    }
  }
}

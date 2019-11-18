import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { debounceTime, tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';

@Component({
  selector: 'app-column-selector-dropdown',
  templateUrl: './column-selector-dropdown.component.html',
  styleUrls: ['./column-selector-dropdown.component.scss']
})
export class ColumnSelectorDropdownComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  public readonly COLUMN_SELECTOR_DROPDOWN: string = 'columnselectordropdown';
  @Input() label: string = this.COLUMN_SELECTOR_DROPDOWN;
  @Input() defaultvalue: string = null;
  @Input() columns: KeyValue<string>[] = [];
  @Input() enableEmptyOption = true;
  @Output() onSelection: EventEmitter<KeyValue<string>> = new EventEmitter<KeyValue<string>>();

  public form: FormGroup;
  public dropDownController: FormControl;

  private triggerOnSelection = new EventEmitter<KeyValue<string>>();
  private chainOnSelection = this.triggerOnSelection.pipe(
    tap(select => {
      this.onSelection.emit(select);
    })
  );

  private selection: KeyValue<string>;

  constructor() {
  }

  ngOnInit() {
    const formControlObject: any = {};
    this.dropDownController = new FormControl(this.COLUMN_SELECTOR_DROPDOWN, {updateOn: 'blur'});
    if (this.defaultvalue) {
      this.dropDownController.setValue(this.defaultvalue);
    }
    this.dropDownController.valueChanges.pipe(
      debounceTime(16),
      tap(value => {
        this.form.updateValueAndValidity();
        this.selection = value;
        this.triggerOnSelection.emit(value);
      }))
      .subscribe();
    this.chainOnSelection.subscribe();
    this.dropDownController.markAsTouched();
    formControlObject.columnselectordropdown = this.dropDownController;
    this.form = new FormGroup(formControlObject);
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      switch (propName) {
        case 'defaultvalue':
          if (this.dropDownController != null) {
            this.dropDownController.setValue(changes[propName].currentValue, {emitEvent: false});
          }
          break;
        case 'columns':
          if (changes[propName].currentValue == null) {
            this.columns = [];
          }
          break;
      }
    }
  }
}

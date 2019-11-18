import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { DxTreeViewComponent } from 'devextreme-angular';
import { Logger } from '../../classes/logger';
import { Filter } from '../../models/filter';
import { HierarchicalData } from '../../models/hierarchical-data';
import { IIntegration, Integration } from '../../models/integration';
import { ProxyService } from '../../services/proxy.service';

@Component({
  selector: 'app-create-filter',
  templateUrl: './create-filter.component.html',
  styleUrls: ['./create-filter.component.scss']
})
export class CreateFilterComponent implements OnInit, OnChanges {
  private logger = new Logger(this);

  public get fieldLabel() {
    if (this.selectedData.length > 0) {
      return this.selectedData[0].value;
    }
    return null;
  }

  @ViewChild(DxTreeViewComponent, null) treeView;

  @Input('data') hierarchicalData: HierarchicalData[] = [];
  @Output() onApply = new EventEmitter<Filter>();

  @Input() suggestions: any[];
  @Output() suggestionsChange: EventEmitter<any[]>;

  @Input() integration: IIntegration;

  public Operators = [
    'is',
    'contains',
    'is not',
    'exists',
    'does not exist'
  ];
  public field = null;
  public operator = null;
  public fitlerValue = null;
  public selectedData: HierarchicalData[] = [];
  public isDropDownBoxOpened = true;

  ddwHeight = 300;

  public query = '';

  constructor(private proxyService: ProxyService, private ref: ChangeDetectorRef) {
    this.suggestions = [];
    this.suggestionsChange = new EventEmitter<any[]>();

    this.integration = new Integration();
  }

  ngOnInit() {
    this.operator = this.Operators[0];
    this.updateSuggestions(this.fieldLabel, '');
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.data) {
      this.ref.detectChanges();
    }
  }

  apply() {
    if (this.selectedData.length <= 0) {
      return;
    }
    const filter = new Filter({field: this.fieldLabel, operator: this.operator, value: this.fitlerValue});
    this.onApply.emit(filter);
  }

  onFilterInput(event) {
    this.query = event;
    this.updateSuggestions(this.fieldLabel, this.query);
  }

  treeView_itemSelectionChanged(e) {
    if (e.itemData.value != null) {
      this.logger.debug('error.itemData.value', e);
      const itemData: HierarchicalData = new HierarchicalData({
        items: [],
        selected: false,
        id: e.itemData.id,
        value: e.itemData.value,
        display: e.itemData.display
      });
      this.field = itemData.id;
      this.selectedData = [];
      this.selectedData.push(itemData);
      this.closeDropdownBox();
    } else {
      this.field = null;
    }
    if (this.selectedData.length > 0) {
      if (this.selectedData[0].selected) {
        this.updateSuggestions(this.fieldLabel, this.query);
      }
    }
    this.logger.debug(this.field, e);
  }

  closeDropdownBox() {
    this.isDropDownBoxOpened = false;
  }

  private updateSuggestions(fieldName, value): Promise<void> {
    return this.proxyService.completion(fieldName, value, this.integration._ids).then(response => {
      const newSuggestions = [];
      for (const term of response.aggregations.suggested_terms.buckets) {
        newSuggestions.push(term.key);
      }
      if (newSuggestions.length <= 0) {
        for (const term of response.suggest.simple_phrase[0].options) {
          newSuggestions.push(term.text);
        }
      }
      this.suggestionsChange.emit(newSuggestions);
    });
  }
}

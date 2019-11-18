import { sprintf } from 'sprintf-js';
import { Logger } from '../../classes/logger';

export interface IEpicUIColumn {
  text: string;
  variable_name: string;
  selected: boolean;
}

export class EpicUIColumn implements IEpicUIColumn {
  constructor(
    public text = '',
    public variable_name: string = '',
    public selected = false
  ) {
  };
}

export interface IEpicUIModel {
  type: string;
  selectAll: boolean;
  itemsSelected: number;
  itemsSelectedText: string;
  columns: IEpicUIColumn[];
}

export class EpicUiModel implements IEpicUIModel {
  private logger = new Logger(this);

  constructor(
    public type = '',
    public selectAll = false,
    public itemsSelected = 0,
    public itemsSelectedText = '',
    public columns: IEpicUIColumn[] = []
  ) {
    this.updateItemsSelected();
    this.updateDataText();
  };

  Initialize(model: IEpicUIModel) {
    for (let p in model) {
      this[p] = model[p];
    }
    this.updateItemsSelected();
    this.updateDataText();
  }

  updateItemsSelected() {
    this.itemsSelected = 0;
    this.columns.forEach(column => {
      if (column.selected) {
        ++this.itemsSelected;
      }
    });
  }

  updateDataText() {
    this.itemsSelected = Math.min(this.columns.length, Math.max(0, this.itemsSelected));
    this.logger.debug('Math', Math.min(this.columns.length, Math.max(0, this.itemsSelected)));

    this.selectAll = false;
    this.itemsSelectedText = '';

    if (this.itemsSelected > 0) {
      this.selectAll = undefined;
      this.itemsSelectedText = sprintf('%s items integration', this.itemsSelected);

      if (this.itemsSelected >= this.columns.length) {
        this.selectAll = true;
        this.itemsSelectedText = sprintf('All items integration');
      }
    }

    this.logger.debug('data.itemsSelectedText', this.itemsSelected, this.itemsSelectedText);

  }

  // set Defaults here
  toJSON(): any {
    return {
      'type': this.type,
      'selectAll': this.selectAll,
      'itemsSelected': this.itemsSelected,
      'itemsSelectedText': this.itemsSelectedText,
      'columns': this.columns
    };
  }
}

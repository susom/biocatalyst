export interface IHierarchicalData {
  id: string;
  value: string;
  display: string;
  items: IHierarchicalData[];
  selected: boolean;
}

export class HierarchicalData implements IHierarchicalData {
  public id: string;
  public value: string;
  public display: string;
  public items: IHierarchicalData[];
  public selected: boolean;

  constructor(source?: IHierarchicalData) {
    this.id = source ? source.id : '';
    this.value = source ? source.value : '';
    this.display = source ? source.display : '';
    this.items = source ? Array.from(source.items, (x) => new HierarchicalData(x)) : [];
    this.selected = source ? source.selected : false;
  }
}

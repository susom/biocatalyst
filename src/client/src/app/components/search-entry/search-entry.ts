export interface ISearchEntry {
  title: string;
  description: string;
  id: string;
}

export class SearchEntry implements ISearchEntry {

  constructor(
    public title: string,
    public description: string,
    public id: string
  ) {
  }

}

export class Overwrite {
  constructor(
    public value = false,
    public message = '') {
  }

  public cancel() {
  }

  public confirm() {
  }

  public reset() {
    this.value = false;
    this.message = '';
  }
}

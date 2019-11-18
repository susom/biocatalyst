export class Pair<T0, T1> {
  protected one: T0;
  protected two: T1;

  public constructor(One?: T0, Two?: T1) {
    this.one = One;
    this.two = Two;
  }

  public set first(Value: T0) {
    this.one = Value;
  }

  public get first(): T0 {
    return this.one;
  }

  public set second(Value: T1) {
    this.two = Value;
  }

  public get second(): T1 {
    return this.two;
  }

  Reset(): void {
    this.one = null;
    this.two = null;
  }

  Release(): void {
    this.one = undefined;
    this.two = undefined;
  }

  Obliterate(): void {
    this.one = undefined;
    this.two = undefined;

    delete this.one;
    delete this.two;
  }
}

export interface IKeyValue<T> {
  key: string;
  value: T;
}

export class KeyValue<T> extends Pair<string, any> implements IKeyValue<T> {
  protected two: T;

  public set key(Value: string) {
    this.one = Value;
  }

  public get key(): string {
    return this.one;
  }

  public set value(Value: any) {
    this.two = Value;
  }

  public get value(): any {
    return this.two;
  }
}


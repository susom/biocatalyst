export interface IDateRange {
  start: string;
  end: string;
}

export interface INumericRange {
  gte: any;
  lt: any;
}

export class NumericRange implements INumericRange {
  public static isNull(range: INumericRange) {
    if (range.gte == null) {
      if (range.lt == null) {
        return true;
      }
    }
    return false;
  }

  constructor(
    public gte: any = null,
    public lt: any = null) {
  }
}

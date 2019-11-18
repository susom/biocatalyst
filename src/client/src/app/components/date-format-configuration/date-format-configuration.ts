import { EventEmitter } from '@angular/core';

export interface IDateFormatConfiguration {
  configuration: string,
  sourceId: string,
  defaultValue: string;
  onChange: EventEmitter<string>;
}

export class DateFormatConfiguration implements IDateFormatConfiguration {
  constructor(
    public configuration: string = null,
    public sourceId: string = null,
    public defaultValue: string = null,
    public onChange = new EventEmitter<string>()
  ) {
  }

  public static Copy(A: IDateFormatConfiguration, B: IDateFormatConfiguration) {
    // Copies A into B
    for (let property in A) {
      B[property] = A[property];
    }
  }

  public static ConvertMomentFormatToBackendFormat(format: string) {
    if (!format) {
      return null;
    }
    format = format.replace(/YYYY/gi, 'yyyy');
    format = format.replace(/DD/gi, 'dd');
    format = format.replace(/\//gi, '-');
    return format;
  }
}

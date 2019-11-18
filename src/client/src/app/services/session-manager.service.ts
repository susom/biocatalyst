import { Injectable } from '@angular/core';

@Injectable()
export class SessionManager {

  public static readonly DefaultHeaders: string[] = [
    's0_specimenid',
    's0_ppid',
    's0_specimen_type',
    's0_specimen_available_quantity',
    's0_visit_date',
    's0_specimen_label'
  ];

  public static readonly ViewableDefaultHeaders: string[] = [
    's0_ppid',
    's0_specimen_type',
    's0_specimen_available_quantity',
    's0_visit_date',
    's0_specimen_label'
  ];

  public static readonly HiddenHeaders: string[] = [
    'specimenid'
  ];

  public static IsHiddenHeader(Header: string): boolean {
    for (const header of SessionManager.HiddenHeaders) {
      if (header === Header) {
        return true;
      }
    }
    return false;
  }

  public static FormatString(Value: string): string {
    if (!Value) {
      return '';
    }
    let result: string = Value.replace(/rc_/, '');
    result = result.replace(/_/g, ' ');
    return result;
  }
}

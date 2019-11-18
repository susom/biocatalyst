import { Injectable } from '@angular/core';
import * as _ from 'lodash';
import * as moment from 'moment';
import { of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { DateFormatConfigurationComponent } from './date-format-configuration.component';

// Wanted to be more Explicit about where these data dataSources where coming from.
export const REDCapAcceptedFormats = [
  'DD/MM/YYYY', // DATE_DMY 16-02-2011
  'MM/DD/YYYY', // DATE_MDY 02-16-2011
  'YYYY/MM/DD', // DATE_YMD 2011-02-16
  'DD/MM/YYYY HH:mm', // DATETIME_DMY 16-02-2011 17:45
  'MM/DD/YYYY HH:mm', // DATETIME_MDY 02-16-2011 17:45
  'YYYY/MM/DD HH:mm', // DATETIME_YMD 2011-02-16 17:45
  'DD/MM/YYYY HH:mm:ss', // DATETIME_SECONDS_DMY	16-02-2011 17:45:23
  'MM/DD/YYYY HH:mm:ss', // DATETIME_SECONDS_MDY	02-16-2011 17:45:23
  'YYYY/MM/DD HH:mm:ss' // DATETIME_SECONDS_YMD	2011-02-16 17:45:23
];

// Redcap is apparently using slashes in some cases.
// YYYY DD MM variation doesn't exist in redcap/is or Epic but is supported.
export const ExtraSupportedFormats = [
  'DD-MM-YYYY', // DATE_DMY 16-02-2011 (Redcap Dash Delim)
  'MM-DD-YYYY', // DATE_MDY 02-16-2011 (Redcap Dash Delim)

  'YYYY/DD/MM', // Extra Variation     (Slash Delim)
  'YYYY-DD-MM', // Extra Variation     (Dash Delim)
  'YYYY-MM-DD', // DATE_YMD 2011-02-16 (Redcap Dash Delim)

  'DD-MM-YYYY HH:mm', // DATETIME_DMY 16-02-2011 17:45 (Redcap Dash Delim)
  'MM-DD-YYYY HH:mm', // DATETIME_MDY 02-16-2011 17:45 (Redcap Dash Delim)

  'YYYY/DD/MM HH:mm', // Extra Variation               (Slash Delim)
  'YYYY-DD-MM HH:mm', // Extra Variation               (Dash Delim)
  'YYYY-MM-DD HH:mm', // DATETIME_YMD 2011-02-16 17:45 (Redcap Dash Delim)

  'DD-MM-YYYY HH:mm:ss', // DATETIME_SECONDS_DMY	16-02-2011 17:45:23 (Redcap Dash Delim)
  'MM-DD-YYYY HH:mm:ss', // DATETIME_SECONDS_MDY	02-16-2011 17:45:23 (Redcap Dash Delim)

  'YYYY/DD/MM HH:mm:ss', // Extra Variation                           (Slash Delim)
  'YYYY-DD-MM HH:mm:ss', // Extra Variation                           (Dash Delim)
  'YYYY-MM-DD HH:mm:ss' // DATETIME_SECONDS_YMD	2011-02-16 17:45:23 (Redcap Dash Delim)
];

export const OpenSpecimenAcceptedFormats = [
  'MMM DD, YYYY', // I don't think this is used: common_date_fmt=MMM dd, yyyy
  // I don't think this is used: common_time_fmt=HH:mm
  'MM-DD-YYYY',    // common_de_fe_date_fmt=mm-dd-yyyy <- used as defualt for open Specimen
  //    same      // common_de_be_date_fmt=MM-dd-yyyy
  'MM-dd-yyyy HH:mm'  // Supposedly true....
];

export const EpicAcceptedFormats = [
  'YYYY-MM-DD', // common_de_fe_date_fmt=mm-dd-yyyy
  'YYYY-MM-DD HH:mm:ss' // common_de_be_date_fmt=MM-dd-yyyy
];

// This class exist mostly for abstraction pourposes.
// The Main component class was getting unwieldy and "wet"
// This allowed for drier, more managable code
// At the moment it is not a generic class. It is specific to pipes
// that the redcapolumndropdownlayouts class uses.
@Injectable()
export class DataFormatConfigurationPipes {
  private static acceptedFormats = [];
  private logger = new Logger(this);

  private static GenerateAcceptedFormats() {
    this.acceptedFormats = [];
    this.acceptedFormats = this.acceptedFormats.concat(REDCapAcceptedFormats);
    this.acceptedFormats = this.acceptedFormats.concat(OpenSpecimenAcceptedFormats);
    this.acceptedFormats = this.acceptedFormats.concat(EpicAcceptedFormats);
    this.acceptedFormats = this.acceptedFormats.concat(ExtraSupportedFormats);
    this.acceptedFormats = _.uniq(this.acceptedFormats);
  }

  constructor() {
    DataFormatConfigurationPipes.GenerateAcceptedFormats();
  }

  public GetPossibleFormats(config: DateFormatConfigurationComponent) {
    return tap((date: string) => {
      let formats = [];
      for (let prop of DataFormatConfigurationPipes.acceptedFormats) {
        if (moment(date, prop, true).isValid()) {
          formats.push(prop);
        }
      }
      // Duplicate formats are expected. Explicitly removing them here.
      formats = _.uniq(formats);
      config.options.next(formats);
    });
  }

  public SelectDefaultValue(config: DateFormatConfigurationComponent) {
    return tap((date: string) => {
      config.selectedItems.next(
        (config.Config.defaultValue) ? [config.Config.defaultValue] : []
      );
    });
  }

  public postSampleDateRetreival(config: DateFormatConfigurationComponent) {
    return tap((date: string) => {
      config.loadingData.next(false);
    });
  }

  public catchSampleDateRetrievalError(config: DateFormatConfigurationComponent) {
    return catchError(error => {
      config.loadingData.next(false);
      return of(error);
    });
  }
}

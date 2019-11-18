import { Injectable } from '@angular/core';
import { pipe } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { ISubjectCodeTypes } from '../../models/tokenization-instructions';
import { DataSourceOpenSpecimenComponent } from './data-source-open-specimen.component';

// This class exist mostly for abstraction pourposes.
// The Main component class was getting unwieldy and "wet"
// This allowed for drier, more managable code
// At the moment it is not a generic class. It is specific to pipes
// that the redcapolumndropdownlayouts class uses.
@Injectable()
export class DataSourceOpenSpecimenPipes {
  private logger = new Logger(this);

  constructor() {
  }

  public ValidateAndSetResults(osDataSource: DataSourceOpenSpecimenComponent) {
    return tap((result: string) => {
      this.logger.debug('Validating and setting results');
      osDataSource.Validation(result);
      osDataSource.source.connection_details.data = result;
    });
  }

  public ResetTokens(osDataSource: DataSourceOpenSpecimenComponent) {
    return tap((result) => {
      // reset important tokens and Dilimeter positions.

      this.logger.debug('reset Tokens Attempt:');
      let tokenInstructions = osDataSource.source.integration_details.subjectCode.find(detail => {
        return detail.type == ISubjectCodeTypes.ppid;
      });
      if (tokenInstructions) {
        tokenInstructions.delimiter_positions = [];
        tokenInstructions.growth_tokens = [];
        tokenInstructions.important_tokens = [];
        tokenInstructions.important_tokens_order = [];
        tokenInstructions.processed_id = null;
        this.logger.debug('reset Tokens');
      }

    });
  }

  public TriggerChanges(osDataSource: DataSourceOpenSpecimenComponent) {
    return pipe(
      tap(result => {
        osDataSource.onChange.emit();
      })
    );
  }
}

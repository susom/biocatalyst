import { Injectable } from '@angular/core';
import { pipe } from 'rxjs';
import { filter, tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { KeyValue } from '../../classes/system/pair';
import { ISubjectCodeTypes } from '../../models/tokenization-instructions';
import { DataSourceRedcapComponent } from './data-source-redcap.component';

// This class exist mostly for abstraction pourposes.
// The Main component class was getting unwieldy and "wet"
// This allowed for drier, more managable code
// At the moment it is not a generic class. It is specific to pipes
// that the redcapolumndropdownlayouts class uses.
@Injectable()
export class DataSourceRedcapPipes {
  private logger = new Logger(this);

  public GetReportsBasedOffProjectID(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      tap((ProjectID: string) => {
        redCapDataSource.source.connection_details.data = ProjectID;
        redCapDataSource.projectReportColumns.next([]);
        redCapDataSource.dataSourceRedcapService.TriggerGetReports(ProjectID);
        // redCapDataSource.triggerChange.Emit(null);

        // Find and add Report ID Layout
        const found = redCapDataSource.connectionDetailsColumnDropdownLayout.find(value => {
          return redCapDataSource.reportsColumnDropdownLayout === value;
        });

        if (ProjectID) {
          if (null == found) {
            redCapDataSource.connectionDetailsColumnDropdownLayout.push(redCapDataSource.reportsColumnDropdownLayout);
          }
        }
      })
    );
  }

  public GenerateAndUpdateProjectLabels(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      tap((projects: any) => {
        // Create KeyValue Labes

        const labels: KeyValue<string>[] = this.GenerateLabelValueListFromObjectArray(
          projects,
          'project_title',
          'project_id'
        );
        // Update projectsColumnDropdownLayout
        redCapDataSource.projectsColumnDropdownLayout.details.columns = labels;
      })
    );
  }

  public GetColumnsBasedOfReportID(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      tap((reportID: string) => {
        redCapDataSource.source.connection_details.credentials.password = reportID;
        redCapDataSource.projectReportColumns.next([]);
        redCapDataSource.dataSourceRedcapService.TriggerGetColumns(
          redCapDataSource.projectId.getValue(),
          reportID
        );
        // redCapDataSource.triggerChange.Emit(null);
      })
    );
  }

  public GenerateAndUpdateReportLabels(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      tap((reports: any) => {
        // Create KeyValue Labels
        const labels: KeyValue<string>[] = this.GenerateLabelValueListFromObjectArray(
          reports,
          'title',
          'report_id'
        );
        // Update projectsColumnDropdownLayout
        redCapDataSource.reportsColumnDropdownLayout.details.columns = labels;
      })
    );
  }

  public GenerateAndUpdateReportColumnLabels(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      tap((reports: any) => {
        // Create KeyValue Labes
        const labels: KeyValue<string>[] = this.GenerateLabelValueListFromObjectArray(
          reports,
          'field_label',
          'field_name'
        );

        // Update projectsColumnDropdownLayout
        redCapDataSource.projectReportColumns.next(labels);
      })
    );
  }

  public ResetTokens(osDataSource: DataSourceRedcapComponent) {
    return tap((result) => {
      // reset important tokens and Dilimeter positions.

      this.logger.debug('reset Tokens Attempt:');
      const tokenInstructions = osDataSource.source.integration_details.subjectCode.find(detail => {
        return detail.type === ISubjectCodeTypes.ppid;
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

  private GenerateLabelValueListFromObjectArray(
    array: any[],
    labelProperty: string,
    fieldProperty: string
  ) {
    const labels: KeyValue<string>[] = [];
    for (const project of array) {
      labels.push(
        new KeyValue<string>(
          project[labelProperty],
          project[fieldProperty]
        )
      );
    }
    return labels;
  }

  public TriggerChanges(redCapDataSource: DataSourceRedcapComponent) {
    return pipe(
      filter(result => {
        return !redCapDataSource.processingSource.getValue();
      }),
      tap(result => {
        redCapDataSource.onChange.emit();
      })
    );
  }
}

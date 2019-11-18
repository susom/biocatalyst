import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DataCollectionTableComponent } from './data-collection-table/data-collection-table.component';
import { EditableTextComponent } from './editable-text/editable-text.component';
import { OpenSpecimenProtocolSelectorComponent } from './open-specimen-protocol-selector/open-specimen-protocol-selector.component';
import { CountDirective } from '../directives/count.directive';
import { FocusDirective } from '../directives/focus.directive';
import { AngularCdkModule } from '../modules/angular-cdk.module';
import { AngularMaterialsModule } from '../modules/angular-materials.module';
import { DevExtremeModule } from '../modules/devextreme.module';
import { DialogsModule } from '../dialogs/dialogs.module';
import { PrimeNGModule } from '../modules/prime-ng.module';
import { ConnectionIconComponent } from './connection-icon/connection-icon.component';
import { CreateFilterComponent } from './create-filter/create-filter.component';
import { DataSourceButtonConfigureIDComponent } from './data-source-button-configure-id/data-source-button-configure-id.component';
import { CsvUploadTableComponent } from './csv-upload-table/csv-upload-table.component';
import { DataSourceCsvComponent } from './data-source-csv/data-source-csv.component';
import { DataSourceEpicComponent } from './data-source-epic/data-source-epic.component';
import { DataSourceFactoryComponent } from './data-source-factory/data-source-factory.component';
import { DataSourceOpenSpecimenComponent } from './data-source-open-specimen/data-source-open-specimen.component';
import { DataSourceRedcapComponent } from './data-source-redcap/data-source-redcap.component';
import { DataSourceTemplateComponent } from './data-source-template/data-source-template.component';
import { ColumnSelectorDropdownComponent } from './column-selector-dropdown/column-selector-dropdown.component';
import { DataSourceListComponent } from './data-source-list/data-source-list.component';
import { EventMappingTableComponent } from './event-mapping-table/event-mapping-table.component';
import { IntegrationConnectionComponent } from './integration-connection/integration-connection.component';
import { DateFormatConfigurationComponent } from './date-format-configuration/date-format-configuration.component';
import { DeleteIconComponent } from './delete-icon/delete-icon.component';
import { ErrorModalComponent } from './error-modal/error-modal.component';
import { FeedbackButtonComponent } from './feedback-button/feedback-button.component';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { ChipsComponent } from './chips/chips.component';
import { DialogTemplateComponent } from './dialog-template/dialog-template.component';
import { MenuBarComponent } from './menu-bar/menu-bar.component';
import { ProgressBarActionComponent } from './progress-bar-action/progress-bar-action.component';
import { SaveReportDialogComponent } from './save-report-dialog/save-report-dialog.component';
import { SideNavIntegrationsComponent } from './side-nav-integrations/side-nav-integrations.component';
import { ReportSelectorComponent } from './study-selector/report-selector/report-selector.component';
import { StudySelectorComponent } from './study-selector/study-selector.component';
import { PivotDataCollectionComponent } from './pivot-table/pivot-table.component';
import { SearchEntryComponent } from './search-entry/search-entry.component';
import { VerifyModalComponent } from './verify-modal/verify-modal.component';

@NgModule({
  imports: [
    AngularCdkModule,
    AngularMaterialsModule,
    CommonModule,
    FormsModule,
    DialogsModule,
    PrimeNGModule,
    ReactiveFormsModule,
    DevExtremeModule
  ],
  exports: [
    ConnectionIconComponent,
    DeleteIconComponent,
    EditableTextComponent,
    ErrorModalComponent,
    VerifyModalComponent,
    FileUploadComponent,
    ChipsComponent,
    DataSourceListComponent,
    DataSourceCsvComponent,
    DataSourceFactoryComponent,
    DataSourceOpenSpecimenComponent,
    DataSourceRedcapComponent,
    DialogTemplateComponent,
    DataSourceTemplateComponent,
    MenuBarComponent,
    ProgressBarActionComponent,
    ReportSelectorComponent,
    SaveReportDialogComponent,
    SideNavIntegrationsComponent,
    DataSourceEpicComponent,
    StudySelectorComponent,
    OpenSpecimenProtocolSelectorComponent,
    PivotDataCollectionComponent,
    SearchEntryComponent,
    IntegrationConnectionComponent,
    FocusDirective,
    CreateFilterComponent,
    CountDirective,
    CsvUploadTableComponent,
    ColumnSelectorDropdownComponent,
    EventMappingTableComponent,
    DateFormatConfigurationComponent,
    FeedbackButtonComponent,
    DataCollectionTableComponent
  ],
  declarations: [
    CsvUploadTableComponent,
    ConnectionIconComponent,
    DeleteIconComponent,
    EditableTextComponent,
    ErrorModalComponent,
    VerifyModalComponent,
    FileUploadComponent,
    DataSourceTemplateComponent,
    ChipsComponent,
    DataSourceListComponent,
    DataSourceCsvComponent,
    DataSourceFactoryComponent,
    DataSourceOpenSpecimenComponent,
    DataSourceRedcapComponent,
    DialogTemplateComponent,
    MenuBarComponent,
    ProgressBarActionComponent,
    ReportSelectorComponent,
    SaveReportDialogComponent,
    SideNavIntegrationsComponent,
    StudySelectorComponent,
    OpenSpecimenProtocolSelectorComponent,
    PivotDataCollectionComponent,
    SearchEntryComponent,
    DataSourceEpicComponent,
    FocusDirective,
    CreateFilterComponent,
    IntegrationConnectionComponent,
    CountDirective,
    ColumnSelectorDropdownComponent,
    EventMappingTableComponent,
    DataSourceButtonConfigureIDComponent,
    DateFormatConfigurationComponent,
    FeedbackButtonComponent,
    DataCollectionTableComponent
  ],
  entryComponents: [
    ErrorModalComponent,
    VerifyModalComponent,
    ReportSelectorComponent,
    SaveReportDialogComponent,
    EventMappingTableComponent,
    DateFormatConfigurationComponent
  ]
})
export class AppComponentsModule {
}


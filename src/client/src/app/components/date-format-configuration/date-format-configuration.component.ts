import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { BehaviorSubject } from 'rxjs';
import { ProxyService } from '../../services/proxy.service';
import { DataFormatConfigurationPipes } from './data-format-configuration.pipes';
import { DateFormatConfiguration, IDateFormatConfiguration } from './date-format-configuration';

@Component({
  selector: 'app-date-format-configuration',
  templateUrl: './date-format-configuration.component.html',
  styleUrls: ['./date-format-configuration.component.scss'],
  providers: [DataFormatConfigurationPipes]
})
export class DateFormatConfigurationComponent implements OnInit {
  private config = new DateFormatConfiguration();
  public options: BehaviorSubject<any> = new BehaviorSubject([]);
  public selectedItems: BehaviorSubject<string[]> = new BehaviorSubject([]);
  public loadingData: BehaviorSubject<boolean> = new BehaviorSubject(true);

  constructor(
    @Inject(MAT_DIALOG_DATA) public injectable: IDateFormatConfiguration,
    private proxyService: ProxyService,
    private pipes: DataFormatConfigurationPipes) {
    // Ingest injectable
    if (injectable) {
      DateFormatConfiguration.Copy(injectable, this.config);
    }
  }

  ngOnInit() {

    this.proxyService.getSampleDate(
      this.config.configuration,
      this.config.sourceId)
      .pipe(
        this.pipes.GetPossibleFormats(this),
        this.pipes.SelectDefaultValue(this),
        this.pipes.postSampleDateRetreival(this),
        this.pipes.catchSampleDateRetrievalError(this)
      ).subscribe();

  }

  public get Config() {
    return this.config;
  }

  public onValueChanged(value) {
    this.config.onChange.emit(value.addedItems[0]);
  }
}

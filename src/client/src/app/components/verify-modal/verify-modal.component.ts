import { Component, ElementRef, EventEmitter, Inject, OnInit, Renderer2 } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { BehaviorSubject, Subscription } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { IDataSource } from '../../models/data-source';
import { IIntegration } from '../../models/integration';
import { IntegrationNavigationService } from '../../services/integration-navigation.service';
import { ProxyService } from '../../services/proxy.service';
import { IVerifyModalObject, VerifyModalObject } from './verify-modal.interface';

@Component({
  selector: 'app-verify-modal',
  templateUrl: './verify-modal.component.html',
  styleUrls: ['./verify-modal.component.scss']
})
export class VerifyModalComponent implements OnInit {
  private logger: Logger = new Logger(this);

  public static readonly Important = 'important';
  public static readonly Split = 'split';
  public static readonly Complete = 'complete';
  public static readonly NewString = 'newstring';

  public model: IVerifyModalObject;

  public divider = '+';
  public integration: IIntegration;
  public dataSources = new BehaviorSubject<IDataSource[]>([]);

  public IDStep: string;
  public tokenSelected = false;
  public processed_id_formatted: string;

  public dividerLocations = [];
  public growth_token_string: string = null;

  private getPreprocessedID = new EventEmitter();

  private preProcessedIDs = this.getPreprocessedID.pipe(
    filter(args => {
      if (this.model.processed_id == null) {
        this.model.processed_id = '';
      }

      this.model.tokens = Array.from(this.model.processed_id);
      return this.model.processed_id.length <= 0;
    }),
    switchMap(args => {
      this.logger.debug('Getting prepocessed IDs:', args);
      return this.proxyService.getPreprocessedID(args[0], args[1]);
    }))

    .subscribe((value: any) => {
      this.model.tokens = Array.from(value.raw);
      this.goToStep(VerifyModalComponent.Split);
    });

  private evaluateAgainstSource = new EventEmitter();
  private evaluateIDs = this.evaluateAgainstSource.pipe(
    filter(args => !this.model.isPrimarySource),
    switchMap(args => {
      this.logger.debug('Getting evaluateIDs IDs:', args);
      return this.proxyService.evaluateID(args[0], args[1]);
    })
  ).subscribe((value: any) => {
    this.model.matching_strategy = value.matching_strategy;
    this.model.important_tokens_order = value.important_tokens_order;
    this.goToStep('evaluationComplete');
    // If Satisfactory
    this.Close();
  });

  constructor(
    private proxyService: ProxyService,
    private renderer: Renderer2,
    private elem: ElementRef,
    @Inject(MAT_DIALOG_DATA) public dataObject: IVerifyModalObject,
    public dialogRef: MatDialogRef<VerifyModalComponent>,
    private integrationNavigation: IntegrationNavigationService) {
    this.model = new VerifyModalObject(dataObject);
  }

  addDivider(event) {
    if (event.srcElement.innerHTML === '+') {
      //// change text to divider and add active class
      event.srcElement.classList.add('active');
      event.target.parentNode.classList.add('active');
      this.logger.debug('ADD DIVIDER');
    } else if (event.srcElement.innerHTML === '-') {
      //// change text back to + and remove active class
      event.srcElement.innerHTML = '+';
      event.srcElement.classList.remove('active');
      event.target.parentNode.classList.remove('active');
    }

    // Get array location of (+) clicked, add - in array at that location
    const target = event.target || event.srcElement || event.currentTarget;
    const arrayLocation = parseInt(target.parentNode.id, 10) + 1;

    const newArray = this.model.tokens;
    const insertAtIndex = arrayLocation;
    const stringToBeInserted = '-';

    newArray.splice(insertAtIndex, 0, stringToBeInserted);
    this.model.tokens = newArray;
    this.logger.debug('addDivider', this.model.tokens);
  }

  removeDivider(event) {
    if (event.srcElement.innerHTML === '-') {
      const target = event.target || event.srcElement || event.currentTarget;
      const arrayLocation = parseInt(target.id, 10);
      this.model.tokens.splice(arrayLocation, 1);
      this.logger.debug('REMOVE DIVIDER', this.model.tokens);
    }
  }

  selectRelevant(event, newValue) {
    // Merge tokens into single array
    const merged = [].concat.apply([], this.model.tokens);

    // Find array location of integration Token
    const importantLocation = merged.indexOf(newValue);

    if (event.srcElement.innerHTML === 'Mark Relevant') {
      //// change text to divider and add active class
      event.srcElement.classList.add('active');
      event.srcElement.parentNode.classList.add('active');
      event.srcElement.innerHTML = `<i class="fa fa-check"></i> Relevant`;
      this.model.important_tokens.push(importantLocation);
      this.logger.debug('Mark Relevant: ' + importantLocation);

    } else if (event.srcElement.innerHTML === `<i class="fa fa-check"></i> Relevant`) {
      //// change text back to + and remove active class
      event.srcElement.innerHTML = 'Mark Relevant';
      event.srcElement.classList.remove('active');
      event.srcElement.parentNode.classList.remove('active');

      if (this.model.important_tokens.indexOf(importantLocation) > -1) {
        this.model.important_tokens.splice(this.model.important_tokens.indexOf(importantLocation), 1);
        this.logger.debug('Remove ' + importantLocation + ' from relevant tokens');
      }
    }
    // Enable Done button if at least one token integration
    if (this.model.important_tokens.length !== 0) {
      this.tokenSelected = true;
    } else {
      this.tokenSelected = false;
    }
  }

  selectGrowthToken(event) {
    this.model.growth_tokens = []; // reset array since currently only 1 token allowed
    if (this.growth_token_string != null) {
      this.model.growth_tokens.push(this.growth_token_string);
    }
  }

  public goToStep(x) {
    this.IDStep = x;

    if (x === VerifyModalComponent.NewString) {
      this.model.processed_id = '';
      this.getPreprocessedID.emit([this.model.config_id, this.model.source_id]);
      this.goToStep(VerifyModalComponent.Split);
    }

    if (x === VerifyModalComponent.Important) {
      this.model.important_tokens.length = 0;
      // add inactive class to all .token after clicking Done
      const uiTokens = document.querySelectorAll('.token-parent');

      for (let i = 0; i < uiTokens.length; i++) {
        // if class is not already found
        if ((' ' + uiTokens[i].className + ' ').indexOf('inactive') < 0) {
          // add inactive class
          uiTokens[i].className += ' inactive';
        }
      }

      // Merge into single array
      const merged = [].concat.apply([], this.model.tokens);

      // Convert to string
      this.model.processed_id = merged.join('');

      // Find array location of each '-'
      const dividerChar = '-';
      let idx = merged.indexOf(dividerChar);

      const tempArray = [];

      while (idx !== -1) {
        tempArray.push(idx);
        idx = merged.indexOf(dividerChar, idx + 1);
      }

      // Decrement by the index value
      for (let i = 0; i < tempArray.length; i++) {
        this.dividerLocations.push(tempArray[i] - i);
      }

      this.model.tokens = this.model.processed_id.split('-');
      this.logger.debug('Divider Locations: ' + this.dividerLocations);
    }

    if (x == VerifyModalComponent.Complete) {
      // Loop through important tokens and create updated processed_id string with bolded/highlighted important tokens
      if (this.model.processed_id) {
        const processed_id_array = this.model.processed_id.split('-');
        const important_token_values = [];

        this.model.important_tokens.forEach((token) => {
          for (let i = 0; i < processed_id_array.length; i++) {
            if (i === token) { // truthy because one is string other is number
              important_token_values.push(processed_id_array[i]);
            }
          }
        });
        // Update array of processed_id to wrap important values with strong tag, then convert back to string
        if (important_token_values) {
          important_token_values.forEach((match) => {
            const index = processed_id_array.indexOf(match);
            if (index !== -1) {
              processed_id_array[index] = '<strong>' + processed_id_array[index] + '</strong>';
            }
          });
          this.processed_id_formatted = processed_id_array.join('-');
          console.log('Highlighted Tokens: ' + this.processed_id_formatted);
        } else {
          this.processed_id_formatted = this.model.processed_id;
        }
      }
    }

  }

  evaluate() {
    this.goToStep('evaluating');
    if (this.model.isPrimarySource) {
      this.Save();
      this.Close();
      return;
    }

    if (!this.model.isPrimarySource) {
      this.EmitAfterSave(this.evaluateAgainstSource, [this.model.config_id, this.model.source_id]);
    }
  }

  private Save() {
    this.model.saveEventStart.emit({
      matching_strategy: this.model.matching_strategy,
      important_tokens: this.model.important_tokens,
      delimiter_positions: this.dividerLocations,
      important_tokens_order: (this.model.isPrimarySource) ? this.GeneratePrimaryTokenOrder() : this.model.important_tokens_order,
      processed_id: this.model.processed_id,
      growth_tokens: this.model.growth_tokens
    });
  }

  private GeneratePrimaryTokenOrder(): number[] {
    const result: number[] = [];
    for (let index = 0; index < this.model.important_tokens.length; index++) {
      result[index] = index;
    }
    return result;
  }

  private EmitAfterSave(event: EventEmitter<any>, args: any[]) {
    const sub: Subscription = this.model.saveEventCompleted.subscribe(() => {
      event.emit(args);
      sub.unsubscribe();
    });
    this.Save();
  }

  public Close() {

    this.logger.debug('afterClosed');

    this.Save();
    this.model.isComplete.emit({
      matching_strategy: this.model.matching_strategy,
      important_tokens: this.model.important_tokens,
      delimiter_positions: this.dividerLocations,
      important_tokens_order: (this.model.isPrimarySource) ? this.GeneratePrimaryTokenOrder() : this.model.important_tokens_order,
      processed_id: this.model.processed_id
    });
    this.dialogRef.close();
  }

  public Restart() {
    this.goToStep(VerifyModalComponent.NewString);
  }

  public Realign() {
    this.model.tokens = Array.from(this.model.processed_id);
    this.goToStep(VerifyModalComponent.Important);
  }

  ngOnInit() {
    this.integration = this.integrationNavigation.getSelectedIntegration();
    this.integrationNavigation.selectedIntegrationChanged.subscribe(([integration, index]) => {
      this.integration = integration;
    });

    console.log('DATA SOURCES LENGTH: ' + this.integration.dataSources.length);

    if (this.model.important_tokens.length > 0) {
      this.goToStep('complete');
      console.log('PROCESSED ID: ' + this.model.processed_id);
    }
    if (this.model.matching_strategy === 'exact') {
      this.goToStep('complete');
      console.log('matching strategy exact');
    }
    if (this.model.important_tokens.length === 0) {
      this.goToStep(VerifyModalComponent.NewString);
    }
    // Send user back to start over if no processed ID but we do have important tokens
    if (this.model.important_tokens.length > 0 && !this.model.processed_id) {
      this.goToStep(VerifyModalComponent.NewString);
    }
    console.log('Important Tokens: ' + this.model.important_tokens);
    console.log('Modal Step: ' + this.IDStep);
  }
}

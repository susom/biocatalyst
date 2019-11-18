import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { Logger } from '../../classes/logger';
import { CollectionProtocolsService } from '../../services/collection-protocols.service';

@Component({
  selector: 'app-open-specimen-protocol-selector',
  templateUrl: './open-specimen-protocol-selector.component.html',
  styleUrls: ['./open-specimen-protocol-selector.component.scss']
})
export class OpenSpecimenProtocolSelectorComponent implements OnInit {
  @Input('selection') public _selection: string = null;
  @Output() public onChange: EventEmitter<any> = new EventEmitter<any>();
  public _options = [];
  public focused: boolean;
  private previousSelection;
  private subs: Subscription[] = [];

  constructor(
    private collectionProtocols: CollectionProtocolsService,
    public ref: ChangeDetectorRef) {
  }

  public onFocus() {
    this.focused = true;
  }

  public onBlur() {
    this.focused = false;
  }

  ngOnInit() {
    this.subs.push(this.collectionProtocols.protocolsUpdated.pipe(
      map(Data => {
        Logger.console('My Study Configuration  Map', Data);
        // Catch Empty Data
        if (Data == null) {
          return;
        } // Data is Empty
        if (Data.length < 0) {
          return;
        } // Data is Empty

        let options = [];
        let selection = null;
        for (let item of Data) {
          options.push({
            label: item.title,
            value: item.id,
            identifier: item.id,
            title: item.title,
            short_title: item.shortTitle
          });
        }
        return options;
      }))
      .subscribe(Options => {
        // [protocols,selection]

        this._options = Options;
        Logger.console('Study Configurations', this._options);
        this.ref.detectChanges();
      })
    );
    this.previousSelection = this._selection;
  }

  ngOnChanges(changes: SimpleChanges) {
    for (let propName in changes) {
      if (propName == '_selection') {
        this.ref.detectChanges();
      }
    }
  }

  Save_CPID() {
    if (this.previousSelection == this._selection) {
      return;
    }
    this.onChange.emit(this._selection);
  }

  private ngOnDestroy() {
    this.subs.forEach(sub => {
      sub.unsubscribe();
    });
    this.subs = [];
    this.onChange = new EventEmitter<any>();
  }

}

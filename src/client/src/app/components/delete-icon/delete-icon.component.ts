import { AfterViewChecked, ChangeDetectorRef, Component, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { MatTooltip } from '@angular/material';
import { Logger } from '../../classes/logger';

@Component({
  selector: 'app-delete-icon',
  templateUrl: './delete-icon.component.html',
  styleUrls: ['./delete-icon.component.scss']
})
export class DeleteIconComponent implements OnChanges, AfterViewChecked {
  private logger = new Logger(this);

  @ViewChild('tooltipHi', null) tooltipHi: MatTooltip;

  public clicked: boolean;
  public hovering: boolean;
  private showToolTipTimeWindow: boolean;

  constructor(public ref: ChangeDetectorRef) {
    this.clicked = false;
    this.hovering = false;
    this.showToolTipTimeWindow = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.clicked) {
      this.showToolTipTimeWindow = !this.clicked;
    }
    if (changes.hovering) {
      this.showToolTipTimeWindow = this.hovering;
    }
  }

  ngAfterViewChecked(): void {
    if (this.tooltipHi._isTooltipVisible() === false) {
      if (this.showToolTipTimeWindow && !this.clicked && this.hovering) {
        this.tooltipHi.show(0);
        this.ref.detectChanges();
      }
    }
  }
}

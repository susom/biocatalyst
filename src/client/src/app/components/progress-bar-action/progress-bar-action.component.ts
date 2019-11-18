import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Logger } from '../../classes/logger';

@Component({
  selector: 'app-progress-bar-action',
  templateUrl: './progress-bar-action.component.html',
  styleUrls: ['./progress-bar-action.component.scss'],
  animations: [
    trigger('showState', [
      state('shown', style({})),
      transition(':enter', [
        style({
          opacity: 0
        }),
        animate('0ms 125ms')
      ]),
      transition(':leave',
        animate(300, style({
          opacity: 0
        }))
      )
    ])
  ]
})
export class ProgressBarActionComponent {
  private logger = new Logger(this);

  private deleteTimer = 0;
  private deleteSub: Subscription;
  public mouseRelease = true;
  @Output() Action = new EventEmitter<any>();

  public MouseDown() {
    this.logger.debug('MouseDown');
    this.mouseRelease = false;
    this.StartTimer();
  }

  public StartTimer() {
    if (this.deleteSub && !this.mouseRelease) {
      return;
    }
    this.logger.debug('StartTimer');
    this.deleteSub = interval(10)
      .pipe(
        tap(result => {
          this.deleteTimer += 1.5;
          this.logger.debug('deleteTimer', this.deleteTimer);
        })
      )
      .subscribe(result => {
        if (this.deleteTimer > 105) {
          this.StopTimer();
          this.Action.emit();
        }
      });
  }

  public MouseUp() {
    this.logger.debug('MouseUp');
    this.mouseRelease = true;
    this.StopTimer();
  }

  public StopTimer() {
    this.logger.debug('StopTimer');
    if (this.deleteSub) {
      this.deleteSub.unsubscribe();
      this.deleteSub = null;
      this.deleteTimer = 0;
    }
  }
}

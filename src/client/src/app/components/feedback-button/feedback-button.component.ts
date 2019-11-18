import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'feedback-button',
  templateUrl: './feedback-button.component.html',
  styleUrls: ['./feedback-button.component.scss']
})

export class FeedbackButtonComponent implements OnInit {
  public confirmBox: Boolean;
  public selectReason: Boolean;
  public reasons = ['Bug', 'Feature Request', 'Question'];
  selectedReason = 'Bug';

  constructor() {
    this.confirmBox = false;
    this.selectReason = false;
  }

  public launchVerify(type) {
    if (type === 'mailto') {
      this.confirmBox = true;
    }
  }

  public yes() {
    this.selectReason = true;
  }

  public no() {
    this.confirmBox = false;
  }

  ngOnInit() {
  }
}

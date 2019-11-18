import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-dialog-template',
  templateUrl: './dialog-template.component.html',
  styleUrls: ['./dialog-template.component.scss']
})
export class DialogTemplateComponent implements OnInit {
  @Input() enableOptions = true;
  @Output() close = new EventEmitter();

  constructor() {
  }

  ngOnInit() {
  }

}

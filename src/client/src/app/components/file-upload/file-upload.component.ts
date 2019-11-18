import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Output } from '@angular/core';
import * as csv from 'csvtojson';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
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
export class FileUploadComponent {
  @Output() fileUploaded: EventEmitter<[string, any]>;
  public uploading: boolean;

  private fileName: string;
  private contents: any;

  constructor() {
    this.uploading = false;
    this.fileUploaded = new EventEmitter<any>();
  }

  valueChanged(event) {
    this.uploading = true;
    const files = event.value;
    const reader = new FileReader();
    reader.onload = () => this.fileLoaded(reader.result as string);
    this.fileName = files[0].name;
    reader.readAsText(files[0]);
  }

  fileLoaded(rawContents: string) {
    csv().fromString(rawContents).then((contents: any) => {
      this.contents = contents;
      this.fileUploaded.emit([this.fileName, this.contents]);
    });
  }
}

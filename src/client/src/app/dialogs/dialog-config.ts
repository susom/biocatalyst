import { MatDialogConfig } from '@angular/material';

export class DialogConfig extends MatDialogConfig {
  constructor(
    public width: any,
    public height: any,
    public component: any,
    public data
  ) {
    super();
    data['component'] = component;
  }
}

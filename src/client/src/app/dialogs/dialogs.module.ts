import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { AngularCdkModule } from '../modules/angular-cdk.module';
import { AngularMaterialsModule } from '../modules/angular-materials.module';
import { SimpleDialogPortalComponent } from './simple-dialog/simple-dialog-portal/simple-dialog-portal.component';
import { SimpleDialogTemplateComponent } from './simple-dialog/simple-dialog-template/simple-dialog-template.component';

@NgModule({
  imports: [
    CommonModule,
    AngularCdkModule,
    AngularMaterialsModule
  ],
  exports: [
    SimpleDialogPortalComponent,
    SimpleDialogTemplateComponent
  ],
  declarations: [
    SimpleDialogPortalComponent,
    SimpleDialogTemplateComponent
  ],
  entryComponents: [
    SimpleDialogPortalComponent
  ]
})
export class DialogsModule {
}

import { ObserversModule } from '@angular/cdk/observers';
import { OverlayModule } from '@angular/cdk/overlay';
import { PortalModule } from '@angular/cdk/portal';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [
    CommonModule
  ],
  exports: [
    ObserversModule,
    OverlayModule,
    PortalModule
  ],
  declarations: []
})
export class AngularCdkModule {
}

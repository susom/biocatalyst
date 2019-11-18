import { NgModule } from '@angular/core';
import { DropdownModule } from 'primeng/components/dropdown/dropdown';
import { MessagesModule } from 'primeng/components/messages/messages';

@NgModule({
  exports: [
    DropdownModule,
    MessagesModule
  ]
})
export class PrimeNGModule {
}

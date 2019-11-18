import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { ProxyService } from '../services/proxy.service';

@Directive({
  selector: '[getCount]'
})
export class CountDirective implements OnInit {
  @Input('getCount') integrationID: string;

  constructor(
    private el: ElementRef,
    private biosearchProxyService: ProxyService
  ) {
  }

  ngOnInit() {
    this.biosearchProxyService.getCount(this.integrationID).then(results => {
      const resultTotal = results.hits.total;
      if (!resultTotal || resultTotal === 0 || resultTotal === undefined) {
        this.el.nativeElement.innerHTML = '<label class="warning">NO RECORDS FOUND!</label>';
      } else {
        this.el.nativeElement.innerHTML = '<label>' + resultTotal + '</label> Total Records';
      }
    });
  }
}

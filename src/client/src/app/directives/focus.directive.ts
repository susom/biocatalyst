import { AfterViewInit, Directive, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[focus]'
})
export class FocusDirective implements AfterViewInit {
  @Input() focus = true;

  constructor(public el: ElementRef) {
  }

  ngAfterViewInit() {
    if (this.focus) {
      this.el.nativeElement.focus();
    }
  }
}

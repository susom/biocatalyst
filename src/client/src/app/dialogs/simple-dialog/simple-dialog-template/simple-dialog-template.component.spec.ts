import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleDialogTemplateComponent } from './simple-dialog-template.component';

describe('SimpleDialogTemplateComponent', () => {
  let component: SimpleDialogTemplateComponent;
  let fixture: ComponentFixture<SimpleDialogTemplateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SimpleDialogTemplateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleDialogTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

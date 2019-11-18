import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedbackButtonComponent } from './feedback-button.component';

describe('MyButtonComponent', () => {
  let component: FeedbackButtonComponent;
  let fixture: ComponentFixture<FeedbackButtonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FeedbackButtonComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedbackButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

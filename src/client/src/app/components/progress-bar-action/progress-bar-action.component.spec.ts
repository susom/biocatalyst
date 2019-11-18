import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProgressBarActionComponent } from './progress-bar-action.component';

describe('MyProgressBarActionComponent', () => {
  let component: ProgressBarActionComponent;
  let fixture: ComponentFixture<ProgressBarActionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProgressBarActionComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgressBarActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportSelectorComponent } from './report-selector.component';

describe('MyReportSelectorComponent', () => {
  let component: ReportSelectorComponent;
  let fixture: ComponentFixture<ReportSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ReportSelectorComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

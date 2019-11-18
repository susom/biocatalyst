import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SaveReportDialogComponent } from './save-report-dialog.component';

describe('MySaveReportDialogComponent', () => {
  let component: SaveReportDialogComponent;
  let fixture: ComponentFixture<SaveReportDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SaveReportDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveReportDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

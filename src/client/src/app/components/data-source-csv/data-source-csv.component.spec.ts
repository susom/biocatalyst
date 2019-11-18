import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceCsvComponent } from './data-source-csv.component';

describe('DataSourceCsvComponent', () => {
  let component: DataSourceCsvComponent;
  let fixture: ComponentFixture<DataSourceCsvComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceCsvComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceCsvComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceOpenSpecimenComponent } from './data-source-open-specimen.component';

describe('DataSourceOpenSpecimenComponent', () => {
  let component: DataSourceOpenSpecimenComponent;
  let fixture: ComponentFixture<DataSourceOpenSpecimenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceOpenSpecimenComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceOpenSpecimenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

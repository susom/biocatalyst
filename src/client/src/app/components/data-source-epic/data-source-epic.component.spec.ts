import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceEpicComponent } from './data-source-epic.component';

describe('DataSourceEpicComponent', () => {
  let component: DataSourceEpicComponent;
  let fixture: ComponentFixture<DataSourceEpicComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceEpicComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceEpicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

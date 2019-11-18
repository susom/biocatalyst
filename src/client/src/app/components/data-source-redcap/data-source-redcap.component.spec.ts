import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceRedcapComponent } from './data-source-redcap.component';

describe('DataSourceRedcapComponent', () => {
  let component: DataSourceRedcapComponent;
  let fixture: ComponentFixture<DataSourceRedcapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceRedcapComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceRedcapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

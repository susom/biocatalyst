import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DateFormatConfigurationComponent } from './date-format-configuration.component';

describe('DateFormatConfigurationComponent', () => {
  let component: DateFormatConfigurationComponent;
  let fixture: ComponentFixture<DateFormatConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DateFormatConfigurationComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DateFormatConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

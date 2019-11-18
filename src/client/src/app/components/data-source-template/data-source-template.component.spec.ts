import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceTemplateComponent } from './data-source-template.component';

describe('DataSourceRedcapComponent', () => {
  let component: DataSourceTemplateComponent;
  let fixture: ComponentFixture<DataSourceTemplateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceTemplateComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

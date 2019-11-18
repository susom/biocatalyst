import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceButtonConfigureIDComponent } from './data-source-button-configure-id.component';

describe('DataSourceButtonConfigureIDComponent', () => {
  let component: DataSourceButtonConfigureIDComponent;
  let fixture: ComponentFixture<DataSourceButtonConfigureIDComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceButtonConfigureIDComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceButtonConfigureIDComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

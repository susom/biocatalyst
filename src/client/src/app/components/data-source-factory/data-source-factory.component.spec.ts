import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataSourceFactoryComponent } from './data-source-factory.component';

describe('DataSourceFactoryComponent', () => {
  let component: DataSourceFactoryComponent;
  let fixture: ComponentFixture<DataSourceFactoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataSourceFactoryComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataSourceFactoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

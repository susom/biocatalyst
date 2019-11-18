import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataCollectionTableComponent } from './data-collection-table.component';

describe('DataCollectionTableComponent', () => {
  let component: DataCollectionTableComponent;
  let fixture: ComponentFixture<DataCollectionTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataCollectionTableComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataCollectionTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

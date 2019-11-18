import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataCollectionComponent } from './data-collection.component';

describe('DataCollectionComponent', () => {
  let component: DataCollectionComponent;
  let fixture: ComponentFixture<DataCollectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DataCollectionComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

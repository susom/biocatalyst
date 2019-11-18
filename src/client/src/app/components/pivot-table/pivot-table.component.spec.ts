import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PivotDataCollectionComponent } from './pivot-table.component';

describe('PivotDataCollectionComponent', () => {
  let component: PivotDataCollectionComponent;
  let fixture: ComponentFixture<PivotDataCollectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PivotDataCollectionComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PivotDataCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

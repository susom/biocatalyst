import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateFilterComponent } from './create-filter.component';

describe('CreateFilterComponent', () => {
  let component: CreateFilterComponent;
  let fixture: ComponentFixture<CreateFilterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CreateFilterComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

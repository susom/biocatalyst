import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChipsComponent } from './chips.component';

describe('MyChipsComponent', () => {
  let component: ChipsComponent;
  let fixture: ComponentFixture<ChipsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ChipsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChipsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
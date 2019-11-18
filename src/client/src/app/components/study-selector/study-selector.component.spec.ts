import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StudySelectorComponent } from './study-selector.component';

describe('MyStudySelectorComponent', () => {
  let component: StudySelectorComponent;
  let fixture: ComponentFixture<StudySelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [StudySelectorComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StudySelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

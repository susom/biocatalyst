import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenSpecimenProtocolSelectorComponent } from './open-specimen-protocol-selector.component';

describe('OpenspecimenprotocolselectorComponent', () => {
  let component: OpenSpecimenProtocolSelectorComponent;
  let fixture: ComponentFixture<OpenSpecimenProtocolSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenSpecimenProtocolSelectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenSpecimenProtocolSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

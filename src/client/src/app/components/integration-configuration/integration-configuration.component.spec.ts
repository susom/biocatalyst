import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IntegrationConfigurationComponent } from './integration-configuration.component';

describe('MyIntegrationConfigurationComponent', () => {
  let component: IntegrationConfigurationComponent;
  let fixture: ComponentFixture<IntegrationConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IntegrationConfigurationComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

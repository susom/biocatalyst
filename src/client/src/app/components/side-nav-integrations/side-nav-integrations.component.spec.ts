import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SideNavIntegrationsComponent } from './side-nav-integrations.component';

describe('MySidenavStudiesComponent', () => {
  let component: SideNavIntegrationsComponent;
  let fixture: ComponentFixture<SideNavIntegrationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SideNavIntegrationsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SideNavIntegrationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});

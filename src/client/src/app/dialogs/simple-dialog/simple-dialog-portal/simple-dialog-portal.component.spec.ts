import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleDialogPortalComponent } from './simple-dialog-portal.component';

describe('ConnectedOverlayComponent', () => {
  let component: SimpleDialogPortalComponent;
  let fixture: ComponentFixture<SimpleDialogPortalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SimpleDialogPortalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleDialogPortalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

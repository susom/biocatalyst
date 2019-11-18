import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectionIconComponent } from './connection-icon.component';

describe('ConnectionIconComponent', () => {
  let component: ConnectionIconComponent;
  let fixture: ComponentFixture<ConnectionIconComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ConnectionIconComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

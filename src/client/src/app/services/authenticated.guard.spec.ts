/* tslint:disable:no-unused-variable */

import { inject, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthenticationService } from '@globalservices/authentication/authentication.service';
import { AuthenticatedGuard } from './authenticated.guard';

describe('AuthenticatedGuard', () => {
  let AuthenticationServiceStub = {
    isLoggedIn: true,
    user: {name: 'Test User'}
  };

  let RouterStub = {
    isLoggedIn: true,
    user: {name: 'Test User'}
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthenticatedGuard,
        {provide: AuthenticationService, useValue: AuthenticationServiceStub},
        {provide: Router, useValue: RouterStub}]
    });
  });

  it('should ...', inject([AuthenticatedGuard], (service: AuthenticatedGuard) => {
    expect(service).toBeTruthy();
  }));
});

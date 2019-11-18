/* tslint:disable:no-unused-variable */

import { inject, TestBed } from '@angular/core/testing';
import { SessionManager } from './session-manager.service';

describe('GlobalService', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SessionManager
      ]
    });
  });

  it('should ...', inject([SessionManager], (service: SessionManager) => {
    expect(service).toBeTruthy();
  }));
});

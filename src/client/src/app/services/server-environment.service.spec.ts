import { inject, TestBed } from '@angular/core/testing';

import { ServerEnvironmentService } from './server-environment.service';

describe('ServerEnvironmentService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ServerEnvironmentService]
    });
  });

  it('should be created', inject([ServerEnvironmentService], (service: ServerEnvironmentService) => {
    expect(service).toBeTruthy();
  }));
});

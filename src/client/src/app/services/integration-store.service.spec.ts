import { inject, TestBed } from '@angular/core/testing';

import { IntegrationStoreService } from './integration-store.service';

describe('IntegrationStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [IntegrationStoreService]
    });
  });

  it('should be created', inject([IntegrationStoreService], (service: IntegrationStoreService) => {
    expect(service).toBeTruthy();
  }));
});

import { inject, TestBed } from '@angular/core/testing';

import { IntegrationNavigationService } from './integration-navigation.service';

describe('IntegrationNavigationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [IntegrationNavigationService]
    });
  });

  it('should be created', inject([IntegrationNavigationService], (service: IntegrationNavigationService) => {
    expect(service).toBeTruthy();
  }));
});

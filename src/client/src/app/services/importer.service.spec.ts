import { inject, TestBed } from '@angular/core/testing';

import { ImporterService } from './importer.service';

describe('ImporterService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ImporterService]
    });
  });

  it('should be created', inject([ImporterService], (service: ImporterService) => {
    expect(service).toBeTruthy();
  }));
});

import { inject, TestBed } from '@angular/core/testing';

import { ReportStoreService } from './report-store.service';

describe('ReportStoreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportStoreService]
    });
  });

  it('should be created', inject([ReportStoreService], (service: ReportStoreService) => {
    expect(service).toBeTruthy();
  }));
});

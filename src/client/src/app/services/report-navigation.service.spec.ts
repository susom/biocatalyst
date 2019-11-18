import { inject, TestBed } from '@angular/core/testing';

import { ReportNavigationService } from './report-navigation.service';

describe('ReportNavigationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportNavigationService]
    });
  });

  it('should be created', inject([ReportNavigationService], (service: ReportNavigationService) => {
    expect(service).toBeTruthy();
  }));
});

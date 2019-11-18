import { inject, TestBed } from '@angular/core/testing';

import { SearchDataService } from './search-data.service';

describe('ReportService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SearchDataService]
    });
  });

  it('should be created', inject([SearchDataService], (service: SearchDataService) => {
    expect(service).toBeTruthy();
  }));
});

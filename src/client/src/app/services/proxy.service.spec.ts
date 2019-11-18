import { inject, TestBed } from '@angular/core/testing';
import { ProxyService } from './proxy.service';

describe('BiosearchProxyService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProxyService]
    });
  });

  it('should be created', inject([ProxyService], (service: ProxyService) => {
    expect(service).toBeTruthy();
  }));
});

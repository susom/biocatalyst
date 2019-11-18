import { inject, TestBed } from '@angular/core/testing';

import { CollectionProtocolsService } from './collection-protocols.service';

describe('CollectionProtocolsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CollectionProtocolsService]
    });
  });

  it('should be created', inject([CollectionProtocolsService], (service: CollectionProtocolsService) => {
    expect(service).toBeTruthy();
  }));
});

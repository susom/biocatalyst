import { inject, TestBed } from '@angular/core/testing';

import { ModalDialogService } from './modal-dialog.service';

describe('ModalDialogService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModalDialogService]
    });
  });

  it('should be created', inject([ModalDialogService], (service: ModalDialogService) => {
    expect(service).toBeTruthy();
  }));
});

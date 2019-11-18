import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';

import { Logger } from '../../classes/logger';
import { IntegrationStoreService } from '../../services/integration-store.service';
import { ModalDialogService } from '../../services/modal-dialog.service';
import { ProxyService } from '../../services/proxy.service';
import { SearchService } from './search.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
  providers: [SearchService]
})
export class SearchComponent implements OnInit {
  private static readonly Search: string = 'search';
  private logger = new Logger(this);

  public myForm: FormGroup = null;

  constructor(
    private elastic: ProxyService,
    private integrationStore: IntegrationStoreService,
    private modal: ModalDialogService,
    private ref: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private searchService: SearchService
  ) {
  }

  public ngOnInit(): void {
    const formControlObject = {};
    formControlObject[SearchComponent.Search] = new FormControl('');
    this.myForm = new FormGroup(formControlObject);

    this.router.navigate([{outlets: {search: 'results'}}], {relativeTo: this.route}).then(nav => {
      this.route.paramMap.subscribe((params: ParamMap) => {
        const searchValue = params.get('search');
        if (searchValue) {
          this.myForm.get(SearchComponent.Search).setValue(searchValue);
          this.searchService.query = this.myForm.get(SearchComponent.Search).value;
        }
      });
    });
  }

  public onSubmit(): void {
    this.searchService.query = this.myForm.get(SearchComponent.Search).value;
  }
}

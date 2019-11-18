import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RoutesRecognized } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, share } from 'rxjs/operators';

class OptionItem {
  constructor(
    public css: string,
    public name: string,
    public active: boolean,
    public onClick: (event) => void) {
  }
}

@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.scss']
})
export class MenuBarComponent implements OnInit {
  options: OptionItem[] = [];
  routerEvents: Observable<any>;

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.routerEvents = router.events.pipe(
      filter(event => event instanceof RoutesRecognized),
      share()
    );
  }

  public async refreshPage() {
    if (this.router.url !== '/home') {
      return this.navigate('../home');
    }
  }

  ngOnInit() {
    this.pushItem(new OptionItem(
      'fa fa-dashboard icon integration',
      'Dashboard',
      false,
      event => {
        this.navigate('../home');
      }),
      '/home'
    );

    this.pushItem(new OptionItem(
      'fa fa-cog icon integration',
      'Integrate',
      false,
      event => {
        this.navigate('../configuration');
      }),
      '/configuration'
    );

    this.pushItem(new OptionItem(
      'fa fa-table icon integration',
      'Analyze',
      false,
      event => {
        this.navigate('../data-collection');
      }),
      '/data-collection'
    );

    this.pushItem(new OptionItem(
      'fa fa-search icon integration',
      'search',
      false,
      event => {
        this.navigate('../search');
      }),
      '/search'
    );
  }

  private pushItem(option: OptionItem, selectOnRouterUrl: string = null): number {
    const index = this.options.push(option);

    if (selectOnRouterUrl) {
      this.routerEvents.subscribe(event => {
        const currentRoute: RoutesRecognized = event as RoutesRecognized;
        if (currentRoute.urlAfterRedirects === selectOnRouterUrl) {
          this.selected(index - 1);
        }
      });
    }

    if (option.onClick) {
      this.options[index - 1].onClick = option.onClick;
    }

    return index;
  }

  public selected(index: number) {
    this.options.forEach(option => {
      option.css = option.css.split(' ').filter(s => s !== 'selected').join(' ');
      option.active = false;
    });

    this.options[index].css = `${this.options[index].css} selected`;
    this.options[index].active = !this.options[index].active;
  }

  public async navigate(url: string) {
    await this.router.navigate([url], {relativeTo: this.route});
  }
}

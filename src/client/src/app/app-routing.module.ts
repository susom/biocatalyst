import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IntegrationConfigurationComponent } from './components/integration-configuration/integration-configuration.component';
import { DataCollectionComponent } from './pages/data-collection/data-collection.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';
import { SearchComponent } from './pages/search/search.component';
import { SearchResultsComponent } from './pages/search/search-results/search-results.component';
import { VisualizeComponent } from './pages/visualize/visualize.component';
import { AuthenticatedGuard } from './services/authenticated.guard';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'table',
    redirectTo: 'table/',
    pathMatch: 'full'
  },
  {
    path: 'data-collection',
    canActivate: [AuthenticatedGuard],
    component: DataCollectionComponent,
    outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  },
  {
    path: 'home',
    canActivate: [AuthenticatedGuard],
    component: HomeComponent, outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  },
  {
    path: 'search',
    canActivate: [AuthenticatedGuard],
    component: SearchComponent,
    outlet: 'primary',
    data: {animation: 'tiger'},
    children: [
      {
        path: 'results',
        canActivate: [AuthenticatedGuard],
        component: SearchResultsComponent,
        outlet: 'search',
        data: {animation: 'tiger'}
      },
      {
        path: 'index',
        canActivate: [AuthenticatedGuard],
        component: DataCollectionComponent,
        outlet: 'search',
        data: {animation: 'tiger'}
      }
    ]
  },
  {
    path: 'visualize',
    canActivate: [AuthenticatedGuard],
    component: VisualizeComponent,
    outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  },
  {
    path: 'configuration',
    canActivate: [AuthenticatedGuard],
    component: IntegrationConfigurationComponent,
    outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  },
  {
    path: 'login',
    component: LoginComponent,
    outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  },
  {
    path: '**',
    component: PageNotFoundComponent,
    outlet: 'primary',
    data: {
      animation: 'tiger'
    }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

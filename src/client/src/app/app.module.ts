import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ConfigurationService } from './app.configuration';
import { AngularMaterialsModule } from './modules/angular-materials.module';
import { DevExtremeModule } from './modules/devextreme.module';
import { PrimeNGModule } from './modules/prime-ng.module';
import { IntegrationConfigurationComponent } from './components/integration-configuration/integration-configuration.component';
import { AppComponentsModule } from './components/app-components.module';
import { DataCollectionComponent } from './pages/data-collection/data-collection.component';
import { HomeComponent } from './pages/home/home.component';
import { LoadingComponent } from './pages/loading/loading.component';
import { LoginComponent } from './pages/login/login.component';
import { PageNotFoundComponent } from './pages/page-not-found/page-not-found.component';
import { SearchResultsComponent } from './pages/search/search-results/search-results.component';
import { SearchComponent } from './pages/search/search.component';
import { VisualizeComponent } from './pages/visualize/visualize.component';
import { ServiceModule } from './services/service.module';
import { SessionManager } from './services/session-manager.service';

@NgModule({
  declarations: [
    AppComponent,
    IntegrationConfigurationComponent,
    LoadingComponent,
    PageNotFoundComponent,
    SearchComponent,
    DataCollectionComponent,
    VisualizeComponent,
    SearchResultsComponent,
    HomeComponent,
    LoginComponent
  ],
  imports: [
    AngularMaterialsModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ServiceModule,
    DevExtremeModule,
    HttpClientModule,
    AppComponentsModule,
    PrimeNGModule,
    ReactiveFormsModule,
    RouterModule,
    MatDialogModule
  ],
  providers: [
    SessionManager,
    ConfigurationService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

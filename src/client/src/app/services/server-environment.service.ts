import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Configuration, IConfiguration } from '../classes/configuration';

@Injectable()
export class ServerEnvironmentService {
  private readonly configurationLocation = window.location.origin.concat('/cgi-bin/config.pl');
  public newUser = false;

  constructor(private http: HttpClient) {
  }

  public fetchConfiguration(): Promise<IConfiguration> {
    if (environment.production) {
      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });
      return this.http.get<IConfiguration>(this.configurationLocation, {headers}).toPromise();
    }
    return Promise.resolve(new Configuration());
  }
}

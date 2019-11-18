import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Logger } from '../classes/logger';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class Network {
  private logger: Logger = new Logger(this);

  constructor(private http: HttpClient, private authenticationService: AuthenticationService) {
  }

  public get<I>(url: string, username?: string, password?: string): Observable<I> {
    this.logger.debug('Get', url);
    const headers = this.getHeaders(username, password);
    return this.http.get<I>(url, {headers});
  }

  public post<I>(url: string, body: any, username?: string, password?: string): Observable<I> {
    this.logger.debug('Post', url, body);
    const headers = this.getHeaders(username, password);
    return this.http.post<I>(url, body, {headers});
  }

  public put<I>(url: string, body: any, username?: string, password?: string): Observable<I> {
    this.logger.debug('Put', url, body);
    const headers = this.getHeaders(username, password);
    return this.http.put<I>(url, body, {headers});
  }

  public head<I>(url: string, username?: string, password?: string): Observable<I> {
    this.logger.debug('Head', url);
    const headers = this.getHeaders(username, password);
    return this.http.head<I>(url, {headers});
  }

  public patch<I>(url: string, body: any, username?: string, password?: string): Observable<I> {
    this.logger.debug('Patch', url);
    const headers = this.getHeaders(username, password);
    return this.http.patch<I>(url, body, {headers});
  }

  public delete<I>(url: string, username?: string, password?: string): Observable<I> {
    this.logger.debug('Delete', url);
    const headers = this.getHeaders(username, password);
    return this.http.delete<I>(url, {headers});
  }

  private getHeaders(username?: string, password?: string) {
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    if (username) {
      headers = headers.append('Authorization', 'Basic ' + btoa(username + ':' + password));
    } else if (this.authenticationService.check()) {
      headers = headers.append('Authorization', this.authenticationService.getAuthHeader());
    }
    return headers;
  }
}

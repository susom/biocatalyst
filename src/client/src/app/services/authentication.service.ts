import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Logger } from '../classes/logger';

class JwtToken {
}

@Injectable()
export class AuthenticationService {
  private logger: Logger = new Logger(this);
  private loggedIn: boolean;
  private token: JwtToken;

  constructor(private http: HttpClient) {
    this.loggedIn = false;
  }

  async login(username: string, password: string): Promise<boolean> {
    // This is where we are actually going to communicate with the server
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        this.loggedIn = true;
        resolve(this.loggedIn);
      }, 300);
    });
  }

  async logout(): Promise<boolean> {
    this.loggedIn = false;
    return true;
  }

  check(): boolean {
    return this.loggedIn;
  }

  getAuthHeader(): string {
    return `Bearer ${this.token}`;
  }
}

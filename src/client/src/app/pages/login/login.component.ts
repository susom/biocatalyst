import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  isAuthenticating: boolean;
  username: string;
  password: string;

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
  }

  async loginClick() {
    this.isAuthenticating = true;
    const success = await this.authenticationService.login(this.username, this.password);
    if (success) {
      return this.router.navigate(['/home']);
    } else {
      this.isAuthenticating = false;
    }
  }
}

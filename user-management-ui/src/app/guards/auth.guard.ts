import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {AuthService} from "../services/auth.service";

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService,
              private router: Router) {}

  canActivate() {
    const res = this.authService.isLoggedIn();
    if (!res) {
      this.router.navigate(['/login']);
      // show login form
    }
    return res;
  }
}

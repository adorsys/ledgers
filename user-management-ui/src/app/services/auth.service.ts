import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {JwtHelperService} from "@auth0/angular-jwt";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  public url = `${environment.userManagementEndPoint}`;
  private static TOKEN_KEY = 'token'
  private jwtHelper = new JwtHelperService();

  constructor(private http: HttpClient) {
  }

  authorize(credentials: any) {
    return this.http.post(this.url + '/users/authorise2', null, {
      params: credentials as any,
      responseType: 'text'
    });
  }

  login(credentials: any): Observable<boolean> {
    return this.authorize(credentials).pipe(
      map(jwt => {
        if (jwt) {
          localStorage.setItem(AuthService.TOKEN_KEY, jwt);
          return true;
        }
        return false;
      })
    );
  }

  logout() {
    localStorage.removeItem(AuthService.TOKEN_KEY)
  }

  isLoggedIn(): boolean {
    return !this.jwtHelper.isTokenExpired(this.getToken());
  }

  getToken(): string {
    return localStorage.getItem(AuthService.TOKEN_KEY);
  }

}

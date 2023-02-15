/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {catchError, finalize, flatMap, map} from "rxjs/operators";
import {Observable, of} from "rxjs";
import {JwtHelperService} from "@auth0/angular-jwt";
import {Credentials} from "../models/credentials.model";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  public url = `${environment.onlineBankingEndPoint}`;
  private authTokenStorageKey = 'token';
  private jwtHelperService = new JwtHelperService();
  private overridingJwt: string;

  constructor(private http: HttpClient) { }

  authorize(credentials: Credentials): Observable<string> {
    return this.http.post<any>(this.url + '/users/login', null, {params: credentials as any})
      .pipe(
        map(loginResponse => loginResponse.bearerToken.access_token)
      );
  }

  login(credentials: any): Observable<boolean> {
    return this.authorize(credentials).pipe(
      map(jwt => {
        localStorage.setItem(this.authTokenStorageKey, jwt);
        return true;
      }),
      catchError(() => of(false))
    );
  }

  isLoggedIn(): boolean {
    return !this.jwtHelperService.isTokenExpired(this.getAuthorizationToken());
  }

  logout() {
    localStorage.removeItem(this.authTokenStorageKey)
  }

  getAuthorizationToken(): string {
    if (this.overridingJwt) {
      return this.overridingJwt;
    }
    return localStorage.getItem(this.authTokenStorageKey);
  }

  register(user: any) {
    return this.http.post(this.url + '/users/register', null, {params: user});
  }

  runAs(user: Credentials, x: () => Observable<Object>) {
    return this.authorize(user).pipe(
      flatMap(jwt => {
        this.overridingJwt = jwt;
        return x();
      }),
      finalize(() => this.overridingJwt = null)
    );
  }
}

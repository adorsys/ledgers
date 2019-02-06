import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Amount} from "../models/amount.model";
import {Credentials} from "../models/credentials.model";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private url = `${environment.server}`;

  constructor(private http: HttpClient,
              private authService: AuthService) { }

  getAccounts() {
    return this.http.get(this.url + '/accounts');
  }

  createAccount(account: Account) {
    return this.http.post(this.url + '/accounts', account);
  }

  depositCash(accountId: string, amount: Amount, technicalUser: Credentials) {
    return this.authService.runAs(technicalUser,
      () => this.http.post(this.url + `/accounts/${accountId}/cash`, amount));
  }
}

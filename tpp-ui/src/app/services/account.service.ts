import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Amount} from "../models/amount.model";
import {Credentials} from "../models/credentials.model";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private url = `${environment.staffAccessResourceEndPoint}`;

  constructor(private http: HttpClient,
              private authService: AuthService) { }

  getAccounts() {
    return this.http.get(this.url + '/accounts');
  }

  createAccount(userId: string, account: Account) {
    let params = new HttpParams();
    params.append("userID", userId);
    return this.http.post(this.url + '/accounts', account,{params: {userID: userId}});
  }

  depositCash(accountId: string, amount: Amount, technicalUser: Credentials) {
    return this.authService.runAs(technicalUser,
      () => this.http.post(this.url + `/accounts/${accountId}/cash`, amount));
  }
}

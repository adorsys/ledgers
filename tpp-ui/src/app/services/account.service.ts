import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Amount} from "../models/amount.model";

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private url = `${environment.staffAccessResourceEndPoint}`;

  constructor(private http: HttpClient) { }

  getAccounts() {
    return this.http.get(this.url + '/accounts');
  }

  getAccount(id: String) {
    return this.http.get(this.url + '/accounts/' + id);
  }

  createAccount(userId: string, account: Account) {
    return this.http.post(this.url + '/accounts', account,{
      params: {userID: userId}
    });
  }

  depositCash(accountId: string, amount: Amount) {
    return this.http.post(this.url + '/accounts/' + accountId + '/cash', amount);
  }
}

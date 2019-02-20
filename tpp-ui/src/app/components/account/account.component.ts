import { Component, OnInit } from '@angular/core';
import {map} from "rxjs/operators";
import {AccountService} from "../../services/account.service";
import {ActivatedRoute} from "@angular/router";
import {Account} from "../../models/account.model";

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css']
})
export class AccountComponent implements OnInit {

  account: Account;
  accountID: string;

  constructor(
    private accountService: AccountService,
    private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    this.activatedRoute.params
      .pipe(
        map(response => {
          return response.id;
        })
      )
      .subscribe((accountID: string) => {
        this.accountID = accountID;
        this.getAccount();
      });
  }

  getAccount() {
    this.accountService.getAccount(this.accountID)
      .subscribe((account: Account) => {
        this.account = account;
        console.log(this.account);
      })
  }
}

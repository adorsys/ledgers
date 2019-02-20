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

        console.log(this.accountID);
      });
  }


  getAccount() {

  }

}

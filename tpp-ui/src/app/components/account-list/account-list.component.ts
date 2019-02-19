import { Component, OnInit } from '@angular/core';
import {AccountService} from "../../services/account.service";

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {
  accounts: Account;

  constructor(private accountService: AccountService) { }

  ngOnInit() {
    this.getUsers();
    // this.service.getAccounts()
    //   .subscribe(accounts => this.accounts = accounts);
  }


  getUsers(): void {
    this.accountService.getAccounts()
      .subscribe((accounts: Account) => {
        this.accounts = accounts;
      })
  }

}

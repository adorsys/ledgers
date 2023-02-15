/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Component, OnInit } from '@angular/core';
import {AccountService} from "../../services/account.service";

import {Account} from "../../models/account.model"

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {
  accounts: Account[];

  constructor(private accountService: AccountService) { }

  ngOnInit() {
    this.getUsers();
  }


  getUsers(): void {
    this.accountService.getAccounts()
      .subscribe((accounts: Account[]) => {
        this.accounts = accounts;
      })
  }

}

/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Component, OnInit } from '@angular/core';
import {AccountService} from "../../services/account.service";

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {
  private accounts;

  constructor(private service: AccountService) { }

  ngOnInit() {
    this.service.getAccounts()
      .subscribe(accounts => this.accounts = accounts);
  }
}

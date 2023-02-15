/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AccountService} from "../../services/account.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-cash-deposit',
  templateUrl: './cash-deposit.component.html',
  styleUrls: ['./cash-deposit.component.css']
})
export class CashDepositComponent implements OnInit {

  form = new FormGroup({
    'currency': new FormControl({value: 'EUR', disabled: true}, Validators.required),
    'amount': new FormControl('', Validators.required),
  });

  submitted: boolean;
  accountId: string;
  errorMessage: string;

  constructor(private accountService: AccountService,
              private activatedRoute: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
     this.accountId = this.activatedRoute.snapshot.paramMap.get('id');
  }

  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) {
      return;
    }

    this.accountService.depositCash(this.accountId, this.form.getRawValue())
      .subscribe(
        () => this.router.navigate(['/accounts']),
          error => {
                  if (typeof error.error === 'object') {
                    this.errorMessage = error.error.status + ' ' + error.error.error + ': ' + error.error.message;
                  } else {
                    this.errorMessage = error.status + ' ' + error.error
                  }
          });
  }

  get amount() {
    return this.form.get('amount');
  }

  get currency() {
    return this.form.get('currency');
  }
}

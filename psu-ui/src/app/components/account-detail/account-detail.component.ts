import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AccountStatus, AccountType, UsageType} from "../../models/account.model";
import {AccountService} from "../../services/account.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit {
  accountForm = new FormGroup({
    'accountType': new FormControl(null, Validators.required),
    'usageType': new FormControl(null, Validators.required),
    'currency': new FormControl(null, Validators.required),
    'iban': new FormControl(null, Validators.required),
    'bban': new FormControl(null),
    'pan': new FormControl(null),
    'maskedPan': new FormControl(null),
    'bic': new FormControl(null),
    'msisdn': new FormControl(null),
    'name': new FormControl(null),
    'product': new FormControl(null),
    'linkedAccounts': new FormControl(null),
    'details': new FormControl(null),
    'accountStatus': new FormControl(AccountStatus.ENABLED, Validators.required),
  });

  accountTypes = Object.keys(AccountType);
  accountStatuses = Object.keys(AccountStatus);
  usageTypes = Object.keys(UsageType);

  submitted = false;
  errorMessage = null;

  constructor(private accountService: AccountService,
    private router: Router) { }

  ngOnInit() {
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = null;
    if (this.accountForm.invalid) {
      return;
    }
    this.accountService.createAccount(this.accountForm.value)
      .subscribe(() => this.router.navigate(['/accounts']), error => {
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.status + ' ' + error.error.error + ': ' + error.error.message;
        } else {
          this.errorMessage = error.status + ' ' + error.error
        }
      });
  }

  get accountType() {
    return this.accountForm.get('accountType');
  }

  get usageType() {
    return this.accountForm.get('usageType');
  }

  get accountStatus() {
    return this.accountForm.get('accountStatus');
  }

  get iban() {
    return this.accountForm.get('iban');
  }

  get currency() {
    return this.accountForm.get('currency');
  }
}

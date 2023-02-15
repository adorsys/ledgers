/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Component, OnInit } from '@angular/core';
import {User} from "../../../../models/user.model";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../../services/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'edit-account-access',
  templateUrl: './edit-account-access.component.html',
  styleUrls: ['./edit-account-access.component.css']
})
export class EditAccountAccessComponent implements OnInit {

  id: string;
  user: User;

  accountAccessForm: FormGroup;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private router: Router) { }

  ngOnInit() {
    this.accountAccessForm = this.formBuilder.group({
      accountAccesses: this.formBuilder.array([
        this.initAccountAccessData(),
      ])
    });
  }

  initAccountAccessData() {
    return this.formBuilder.group({
      accessType: ['', Validators.required],
      iban: ['', Validators.required]
    })
  }

  addAccountAccess() {
    const control = <FormArray>this.accountAccessForm.controls['accountAccesses'];
    control.push(this.initAccountAccessData());
  }

  removeAccountAccess(i: number) {
    const control = <FormArray>this.accountAccessForm.controls['accountAccesses'];
    control.removeAt(i);
  }

  onSubmit() {
    console.log(this.accountAccessForm.value);
  }

}

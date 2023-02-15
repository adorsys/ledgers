/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Component, OnInit } from '@angular/core';
import {User} from "../../../../models/user.model";
import {UserService} from "../../../../services/user.service";
import {ActivatedRoute, Params} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.css']
})
export class UserEditComponent implements OnInit {
  id: string;
  user: User;

  editUserForm: FormGroup;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.setupUserFormControl();

    this.route.params.subscribe(
      (params: Params) => {
        this.id = params['id'];

        this.userService.getUserById(this.id)
          .subscribe( (user: User) => {
            this.user = user;
          }, error => {
            console.log(error)
          }, () => {
            this.setUserFormValue();
          });
      });
  }

  get formControl() {
    return this.editUserForm.controls;
  }

  setupUserFormControl(): void {
    this.editUserForm = this.formBuilder.group({
      email: [
        {
          value: null,
          disabled: true
        },
        [
          Validators.required,
          Validators.email
        ]
      ],
      login: [
        {
          value: null,
          disabled: true
        },
        Validators.required
      ],
      pin: [
        {
          value: null,
          disabled: true
        },
        [
          Validators.required,
          Validators.minLength(8)
        ]
      ]
    });
  }

  setUserFormValue(): void {
    this.editUserForm.setValue({
      login: this.user.login,
      email: this.user.email,
      pin: this.user.pin
    });
  }

  onSubmit() {
    console.log(this.editUserForm.value);
  }

}

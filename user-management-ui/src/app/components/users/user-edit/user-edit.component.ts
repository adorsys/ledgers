import { Component, OnInit } from '@angular/core';
import {User} from "../../../models/user.model";
import {UserService} from "../../../services/user.service";
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
    private formBuilder: FormBuilder,) {
  }

  ngOnInit() {
    this.getUser();
    this.editUserForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      pin: ['', [Validators.required, Validators.minLength(8)]],
      // password_confirmation: ['', Validators.required],
      login: ['', Validators.required]
    });
  }

  get formControl() {
    return this.editUserForm.controls;
  }

  getUser(): void {
    this.route.params.subscribe(
      (params: Params) => {
        this.id = params['id'];

        this.userService.getUserById(this.id)
          .subscribe( (user: User) => {
            this.user = user;
          });
      });
  }

}

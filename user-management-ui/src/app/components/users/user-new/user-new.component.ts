import { Component, OnInit } from '@angular/core';
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/user.model";
import {ActivatedRoute, Params} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'user-new',
  templateUrl: './user-new.component.html',
  styleUrls: ['./user-new.component.css']
})
export class UserNewComponent implements OnInit {

  id: string;
  user: User;

  userForm: FormGroup;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.setupUserFormControl();

  }

  setupUserFormControl(): void {
    this.userForm = this.formBuilder.group({
      scaUserData: this.formBuilder.array([
        this.initScaData()
      ]),
      email: ['', [Validators.required, Validators.email]],
      login: ['', Validators.required],
      pin: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  get formControl() {
    return this.userForm.controls;
  }

  initScaData() {
    return this.formBuilder.group({
      scaMethod: ['', Validators.required],
      methodValue: ['', Validators.required]
    })
  }

  onSubmit() {
    console.log(this.userForm.value);
    this.userService.createUser(this.userForm.value)
      .subscribe(response => {
        console.log(response);
      });
  }

}

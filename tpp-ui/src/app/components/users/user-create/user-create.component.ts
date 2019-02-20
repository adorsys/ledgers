import { Component, OnInit } from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/user.model";

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.css']
})
export class UserCreateComponent implements OnInit {

  id: string;
  user: User;

  userForm: FormGroup;
  submitted: boolean;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private router: Router,
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
      pin: ['', [Validators.required, Validators.minLength(5)]],
      userRoles: this.formBuilder.array(['CUSTOMER']) // register users with customer role
    });
  }

  get formControl() {
    return this.userForm.controls;
  }

  initScaData() {
    return this.formBuilder.group({
      scaMethod: ['EMAIL', Validators.required],
      methodValue: ['', Validators.required]
    })
  }

  initAccountAccessData() {
    return this.formBuilder.group({
      accessType: ['OWNER', Validators.required],
      iban: ['', Validators.required]
    })
  }

  addScaDataItem() {
    const control = <FormArray>this.userForm.controls['scaUserData'];
    control.push(this.initScaData());
  }

  removeScaDataItem(i: number) {
    const control = <FormArray>this.userForm.controls['scaUserData'];
    control.removeAt(i);
  }

  onSubmit() {
    this.submitted = true;

    if (this.userForm.invalid) {
      return;
    }

    this.userService.createUser(this.userForm.value)
      .subscribe(() => {
        this.router.navigateByUrl('staff-access/users');
      });
  }

}

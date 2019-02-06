import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  userForm = new FormGroup({
    branch: new FormControl('', Validators.required),
    login: new FormControl('', Validators.required),
    email: new FormControl('', Validators.required),
    pin: new FormControl('', Validators.required)
  });
  submitted: boolean;
  errorMessage: string; //TODO: errors handling with error interceptor

  constructor(private service: AuthService, private router: Router) { }

  ngOnInit() {
  }

  onSubmit(branch: HTMLInputElement) {
    this.submitted = true;
    if (this.userForm.invalid) {
      return;
    }

    this.service.register(this.userForm.value, branch.value)
      .subscribe(() => this.router.navigate(['/login']),
          () =>
            this.errorMessage = 'TPP with this login or email exists already');
  }

  get login() { return this.userForm.get('login'); }

  get branch() { return this.userForm.get('branch'); }

  get email() { return this.userForm.get('email'); }

  get pin() { return this.userForm.get('pin'); }
}

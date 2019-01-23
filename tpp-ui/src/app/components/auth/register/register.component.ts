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
    login: new FormControl('', Validators.required),
    email: new FormControl('', Validators.required),
    pin: new FormControl('', Validators.required),
    role: new FormControl('TECHNICAL') // TPPs are registered in Ledgers as users with role "TECHNICAL"
  });
  submitted: boolean;
  errorMessage: string;

  constructor(private service: AuthService, private router: Router) { }

  ngOnInit() {
  }

  onSubmit() {
    this.submitted = true;
    if (this.userForm.invalid) {
      return;
    }

    this.service.register(this.userForm.value)
      .subscribe(() => this.router.navigate(['/login']),
          () =>
            this.errorMessage = 'TPP with this login or email exists already');
  }

  get login() { return this.userForm.get('login'); }

  get email() { return this.userForm.get('email'); }

  get pin() { return this.userForm.get('pin'); }
}

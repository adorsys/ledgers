import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router) { }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      login: ['', Validators.required],
      pin: ['', Validators.required],
      role: ['STAFF'] // check STAFF role by default
    });
    this.authService.logout();
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value)
        .subscribe(
          success => {
            if (success) {
              this.router.navigate(['/']);
            }
          },
          error1 => {
          });
    }
  }
}

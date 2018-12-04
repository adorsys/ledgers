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
    // private formBuilder: FormBuilder,
    private route: ActivatedRoute) {
  }

  ngOnInit() {
    // this.userForm = this.formBuilder.group({
    //   login: ['', Validators.required]
    // });
    this.getUser();
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

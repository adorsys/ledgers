import { Component, OnInit } from '@angular/core';
import {User} from "../../../models/user.model";
import {UserService} from "../../../services/user.service";
import {ActivatedRoute, Params} from "@angular/router";

@Component({
  selector: 'user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.css']
})
export class UserEditComponent implements OnInit {
  id: string;
  user: User;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute) {
  }

  ngOnInit() {
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

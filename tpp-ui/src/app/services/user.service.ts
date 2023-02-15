/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {User} from "../models/user.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public url = `${environment.staffAccessResourceEndPoint}`;

  constructor(private http: HttpClient) { }

  listUsers() {
    let userRole = 'CUSTOMER'; // for now only users with CUSTOMER role
    return this.http.get(this.url + '/users', {
      params:
        {roles: userRole}
    });
  }

  getUser(userId: string) {
    return this.http.get(this.url + '/users/' + userId);
  }

  createUser(user: User) {
    return this.http.post(this.url + '/users', user);
  }

}

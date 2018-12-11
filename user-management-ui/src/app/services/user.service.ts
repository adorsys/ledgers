import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../models/user.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public url = `${environment.userManagementEndPoint}`;

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type':  'application/json',
    })
  };

  constructor(private http: HttpClient) {
  }

  getAll() {
    return this.http.get(this.url + '/all');
  }

  getUserById(id: string) {
    return this.http.get(this.url + '/' + id);
  }

  createUser(user: User) {
    return this.http.post(this.url, JSON.stringify(user));
  }

  getUserByLogin() {

  }
}

import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../models/user.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public url = `${environment.userManagementEndPoint}`;

  constructor(private http: HttpClient) {
  }

  getAll() {
    return this.http.get(this.url + '/all');
  }

  getUserById(id: string) {
    return this.http.get(this.url + '/' + id);
  }

  createUser(user: User) {
    let header = new HttpHeaders();
    header = header
      .append('Content-Type', 'application/json')
      .append('Accept', 'application/json');
    return this.http.post(this.url + '/', JSON.stringify(user), {headers: header});
  }

  getUserByLogin() {

  }
}

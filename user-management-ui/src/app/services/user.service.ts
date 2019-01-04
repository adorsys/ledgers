import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {User} from "../models/user.model";
import {ScaUserData} from "../models/sca-user-data.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  public url = `${environment.userManagementEndPoint}`;

  constructor(private http: HttpClient) {
  }

  getAll() {
    return this.http.get<User[]>(this.url + '/all');
  }

  getUserById(id: string) {
    return this.http.get<User>(this.url + '/' + id);
  }

  createUser(user: User) {
    return this.http.post(this.url + '/', JSON.stringify(user));
  }

  updateScaData(userID: string, scaData: ScaUserData[]) {
    return this.http.put(this.url + '/' + userID + '/sca-data', JSON.stringify(scaData));
  }

  getUserByLogin() {

  }
}

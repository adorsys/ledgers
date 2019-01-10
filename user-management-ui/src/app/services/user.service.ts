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

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }

  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<User[]>(this.url);
  }

  getUserById(id: string) {
    return this.http.get<User>(this.url + '/' + id);
  }

  createUser(user: User) {
    return this.http.post(this.url + '/', JSON.stringify(user));
  }

  updateScaData(userID: string, scaData: ScaUserData[]) {
    console.log(scaData);
    return this.http.put(this.url + '/sca-data', JSON.stringify(scaData), this.httpOptions);
  }
}

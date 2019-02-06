import {ScaUserData} from "./sca-user-data.model";
import {AccountAccess} from "./account-access.model";

export class User {
  id: string;
  email: string;
  login: string;
  branch: string;
  pin: string;
  scaUserData: ScaUserData [];
  accountAccesses: AccountAccess [];
}

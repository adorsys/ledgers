/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

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

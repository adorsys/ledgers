/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {UsersComponent} from "./components/dashboard/users/users.component";
import {UserNewComponent} from "./components/dashboard/users/user-new/user-new.component";
import {AccountsComponent} from "./components/dashboard/accounts/accounts.component";
import {UserEditComponent} from "./components/dashboard/users/user-edit/user-edit.component";
import {EditScaComponent} from "./components/dashboard/users/edit-sca/edit-sca.component";
import {EditAccountAccessComponent} from "./components/dashboard/users/edit-account-access/edit-account-access.component";
import {LoginComponent} from "./components/auth/login/login.component";
import {AuthGuard} from "./guards/auth.guard";

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'users',
        component: UsersComponent
      },
      {
        path: 'users/create',
        component: UserNewComponent
      },
      {
        path: 'users/:id',
        component: UserEditComponent
      },
      {
        path: 'users/:id/authentication-methods',
        component: EditScaComponent
      },
      {
        path: 'users/:id/account-accesses',
        component: EditAccountAccessComponent
      },
      {
        path: 'accounts',
        component: AccountsComponent
      },
    ]
  },
  {
    path: 'login',
    component: LoginComponent
  },

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {}

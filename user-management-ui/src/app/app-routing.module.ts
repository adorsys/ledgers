import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {UsersComponent} from "./components/users/users.component";
import {UserNewComponent} from "./components/users/user-new/user-new.component";
import {AccountsComponent} from "./components/accounts/accounts.component";
import {UserEditComponent} from "./components/users/user-edit/user-edit.component";
import {EditScaComponent} from "./components/users/edit-sca/edit-sca.component";
import {EditAccountAccessComponent} from "./components/users/edit-account-access/edit-account-access.component";
import {LoginComponent} from "./components/auth/login/login.component";

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent
  },
  {
    path: 'users',
    component: UsersComponent
  },
  {
    path: 'accounts',
    component: AccountsComponent
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
    path: 'login',
    component: LoginComponent
  },

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {}

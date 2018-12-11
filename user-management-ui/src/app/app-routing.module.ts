import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {UsersComponent} from "./components/users/users.component";
import {UserNewComponent} from "./components/users/user-new/user-new.component";
import {AccountsComponent} from "./components/accounts/accounts.component";
import {UserEditComponent} from "./components/users/user-edit/user-edit.component";

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
    path: 'users/:id',
    component: UserEditComponent
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {}

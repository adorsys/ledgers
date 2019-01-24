import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {LoginComponent} from "./components/auth/login/login.component";
import {AuthGuard} from "./guards/auth.guard";
import {RegisterComponent} from "./components/auth/register/register.component";
import {AccountListComponent} from "./components/account-list/account-list.component";
import {AccountDetailComponent} from "./components/account-detail/account-detail.component";
import {NotFoundComponent} from "./components/not-found/not-found.component";
import {CashDepositComponent} from "./components/cash-deposit/cash-deposit.component";

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'accounts'
      },
      {
        path: 'accounts',
        component: AccountListComponent
      },
      {
        path: 'accounts/:id',
        component: AccountDetailComponent
      },
      {
        path: 'accounts/new',
        component: AccountListComponent
      },
      {
        path: 'accounts/deposit',
        component: CashDepositComponent
      },

    ]
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'logout',
    redirectTo: 'login'
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {}

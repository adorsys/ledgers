/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './commons/navbar/navbar.component';
import { FooterComponent } from './commons/footer/footer.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SidebarComponent } from './commons/sidebar/sidebar.component';
import { UsersComponent } from './components/dashboard/users/users.component';
import { AccountsComponent } from './components/dashboard/accounts/accounts.component';
import { UserNewComponent } from './components/dashboard/users/user-new/user-new.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { UserEditComponent } from './components/dashboard/users/user-edit/user-edit.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TokenInterceptor} from "./interceptors/token-interceptor";
import { EditScaComponent } from './components/dashboard/users/edit-sca/edit-sca.component';
import { EditAccountAccessComponent } from './components/dashboard/users/edit-account-access/edit-account-access.component';
import { LoginComponent } from './components/auth/login/login.component';
import {AuthGuard} from "./guards/auth.guard";

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    DashboardComponent,
    SidebarComponent,
    UsersComponent,
    AccountsComponent,
    UserNewComponent,
    UserEditComponent,
    EditScaComponent,
    EditAccountAccessComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    AuthGuard,
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

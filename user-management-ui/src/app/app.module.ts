import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './commons/navbar/navbar.component';
import { FooterComponent } from './commons/footer/footer.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SidebarComponent } from './commons/sidebar/sidebar.component';
import { UsersComponent } from './components/users/users.component';
import { AccountsComponent } from './components/accounts/accounts.component';
import { UserNewComponent } from './components/users/user-new/user-new.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { UserEditComponent } from './components/users/user-edit/user-edit.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TokenInterceptor} from "./interceptors/token-interceptor";
import { EditScaComponent } from './components/users/edit-sca/edit-sca.component';
import { EditAccountAccessComponent } from './components/users/edit-account-access/edit-account-access.component';

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
    EditAccountAccessComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.rest.um.domain;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;

import java.util.List;

public class UserTO {
    private String id;
    private String login;
    private String email;
    private String pin;
    private List<LedgerAccountBO> accounts;

    public UserTO() {
    }

    public UserTO(String id, String login, String email, String pin, List<LedgerAccountBO> accounts) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.pin = pin;
        this.accounts = accounts;
    }

    //Getters-Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public List<LedgerAccountBO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<LedgerAccountBO> accounts) {
        this.accounts = accounts;
    }
}

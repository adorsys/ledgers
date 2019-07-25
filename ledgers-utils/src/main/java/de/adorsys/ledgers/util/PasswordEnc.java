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

package de.adorsys.ledgers.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEnc {
    private PasswordEncoder pe = new BCryptPasswordEncoder();

    public String encode(String userId, String password) {
        return pe.encode(concatenate(userId, password));
    }

    private String concatenate(String userId, String password) {
        return userId + password;
    }

    public boolean verify(String userId, String rawPassword, String encodedPassword) {
        return pe.matches(concatenate(userId, rawPassword), encodedPassword);
    }
}

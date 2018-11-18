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

package de.adorsys.ledgers.middleware.rest.domain;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAMethodTO;

public class SCAGenerationRequest {
    private String userLogin;
    private SCAMethodTO method;
    private String opData;
    private String userMessage;
    private int validitySeconds;

    public SCAGenerationRequest() {
    }

    public SCAGenerationRequest(String userLogin, SCAMethodTO method, String opData, String userMessage, int validitySeconds) {
        this.userLogin = userLogin;
        this.method = method;
        this.opData = opData;
        this.userMessage = userMessage;
        this.validitySeconds = validitySeconds;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public SCAMethodTO getMethod() {
        return method;
    }

    public String getOpData() {
        return opData;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public int getValiditySeconds() {
        return validitySeconds;
    }
}

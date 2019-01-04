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

package de.adorsys.ledgers.middleware.api.domain.sca;

import java.util.Objects;

public class AuthCodeDataTO {
    private String userLogin;
    private String scaUserDataId;
    private String opId;
    private String opData;
    private String userMessage;
    private int validitySeconds;

    public AuthCodeDataTO() {
    }

	public AuthCodeDataTO(String userLogin, String scaUserDataId, String opId, String opData, String userMessage,
			int validitySeconds) {
		super();
		this.userLogin = userLogin;
		this.scaUserDataId = scaUserDataId;
		this.opId = opId;
		this.opData = opData;
		this.userMessage = userMessage;
		this.validitySeconds = validitySeconds;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getScaUserDataId() {
		return scaUserDataId;
	}

	public void setScaUserDataId(String scaUserDataId) {
		this.scaUserDataId = scaUserDataId;
	}

	public String getOpData() {
		return opData;
	}

	public void setOpData(String opData) {
		this.opData = opData;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public int getValiditySeconds() {
		return validitySeconds;
	}

	public void setValiditySeconds(int validitySeconds) {
		this.validitySeconds = validitySeconds;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        AuthCodeDataTO that = (AuthCodeDataTO) o;
        return validitySeconds == that.validitySeconds &&
                       Objects.equals(userLogin, that.userLogin) &&
                       Objects.equals(scaUserDataId, that.scaUserDataId) &&
                       Objects.equals(opData, that.opData) &&
                       Objects.equals(opId, that.opId) &&
                       Objects.equals(userMessage, that.userMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userLogin, scaUserDataId, opData, opId, userMessage, validitySeconds);
    }
	

}

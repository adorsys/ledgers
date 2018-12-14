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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((opData == null) ? 0 : opData.hashCode());
		result = prime * result + ((opId == null) ? 0 : opId.hashCode());
		result = prime * result + ((scaUserDataId == null) ? 0 : scaUserDataId.hashCode());
		result = prime * result + ((userLogin == null) ? 0 : userLogin.hashCode());
		result = prime * result + ((userMessage == null) ? 0 : userMessage.hashCode());
		result = prime * result + validitySeconds;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthCodeDataTO other = (AuthCodeDataTO) obj;
		if (opData == null) {
			if (other.opData != null)
				return false;
		} else if (!opData.equals(other.opData))
			return false;
		if (opId == null) {
			if (other.opId != null)
				return false;
		} else if (!opId.equals(other.opId))
			return false;
		if (scaUserDataId == null) {
			if (other.scaUserDataId != null)
				return false;
		} else if (!scaUserDataId.equals(other.scaUserDataId))
			return false;
		if (userLogin == null) {
			if (other.userLogin != null)
				return false;
		} else if (!userLogin.equals(other.userLogin))
			return false;
		if (userMessage == null) {
			if (other.userMessage != null)
				return false;
		} else if (!userMessage.equals(other.userMessage))
			return false;
		if (validitySeconds != other.validitySeconds)
			return false;
		return true;
	}
	
	

}

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

package de.adorsys.ledgers.deposit.api.domain;

import static de.adorsys.ledgers.deposit.api.domain.ResultStatusBO.SUCCESS;

import java.util.List;

public class PaymentResultBO<T> {

    /**
     * A status of execution result. Is used to provide correct answer to TPP.
     */
    private ResultStatusBO responseStatus;

	public PaymentResultBO() {
	}

	public PaymentResultBO(T paymentResult) {
		this.paymentResult = paymentResult;
		setResponseStatus(SUCCESS);
	}

	/**
     * An optional message that can be returned to explain response status in details.
     * XS2A Service may use it to provide the error explanation to TPP
     */
    private List<String> messages;
    
    private T paymentResult;

    public boolean hasError() {
        return responseStatus != SUCCESS;
    }

    public boolean isSuccessful() {
        return responseStatus == SUCCESS;
    }

	public ResultStatusBO getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResultStatusBO responseStatus) {
		this.responseStatus = responseStatus;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public T getPaymentResult() {
		return paymentResult;
	}

	public void setPaymentResult(T paymentResult) {
		this.paymentResult = paymentResult;
	}
	
    
}

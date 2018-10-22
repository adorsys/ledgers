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

package de.adorsys.ledgers.middleware.service.domain.payment;

import java.util.List;

import static de.adorsys.ledgers.middleware.service.domain.payment.ResultStatusTO.SUCCESS;

public class PaymentResultTO<T> {

    /**
     * A status of execution result. Is used to provide correct answer to TPP.
     */
    private ResultStatusTO responseStatus;

    /**
     * An optional message that can be returned to explain response status in details.
     * XS2A Service may use it to provide the error explanation to TPP
     */
    private List<String> messages;

    private T paymentResult;

    public PaymentResultTO() {
    }

    public PaymentResultTO(T paymentResult) {
        this.paymentResult = paymentResult;
        setResponseStatus(SUCCESS);
    }

    public boolean hasError() {
        return responseStatus != SUCCESS;
    }

    public boolean isSuccessful() {
        return responseStatus == SUCCESS;
    }

    public ResultStatusTO getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResultStatusTO responseStatus) {
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
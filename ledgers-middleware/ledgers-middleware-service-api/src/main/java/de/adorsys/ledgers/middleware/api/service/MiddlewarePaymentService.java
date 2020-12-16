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

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;

import java.util.List;

public interface MiddlewarePaymentService {

    //================= PAYMENT INITIATION ========================//

    /**
     * PROC:01 Initiates a payment. Called by the channel layer.
     * <p>
     * This call sets the status RCVD
     *
     * @param scaInfoTO   : SCA information
     * @param payment     : the payment object
     * @param paymentType : the payment type
     * @return : the sca response object.
     */
    SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, PaymentTO payment, PaymentTypeTO paymentType);

    /**
     * Executes a payment
     *
     * @param scaInfoTO   : SCA information
     * @param paymentId     : payment identifier
     * @return : the sca response object.
     */
    SCAPaymentResponseTO executePayment(ScaInfoTO scaInfoTO, String paymentId);

    // ================= SCA =======================================//

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     *
     * @param scaInfoTO : SCA information
     * @param paymentId : the payment id
     * @return : auth response.
     */
    SCAPaymentResponseTO authorizePayment(ScaInfoTO scaInfoTO, String paymentId);

    //============================ Payment Execution ==============================//

    //============================ Payment Status ==============================//

    /**
     * PROC: 04
     * <p>
     * Read the status of a payment. Can be called repetitively after initiation of a payment.
     *
     * @param paymentId : the payment id
     * @return : the transaction status
     */
    TransactionStatusTO getPaymentStatusById(String paymentId);


    //============================ Payment Status ==============================//

    /**
     * Reads and return a payment.
     *
     * @param paymentId : the payment id
     * @return the payment
     */
    PaymentTO getPaymentById(String paymentId);

    /**
     * Checks the possibility of payment cancellation
     *
     * @param scaInfoTO : SCA information
     * @param paymentId : the payment id
     * @return : the auth response object.
     */
    SCAPaymentResponseTO initiatePaymentCancellation(ScaInfoTO scaInfoTO, String paymentId);

    String iban(String paymentId);

    SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId);

    List<PaymentTO> getPendingPeriodicPayments(ScaInfoTO scaInfoTO);

    CustomPageImpl<PaymentTO> getPendingPeriodicPaymentsPaged(ScaInfoTO scaInfo, CustomPageableImpl pageable);
}

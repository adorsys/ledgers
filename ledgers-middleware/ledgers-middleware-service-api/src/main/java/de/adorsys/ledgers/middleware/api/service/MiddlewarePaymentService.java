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
    SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, Object payment, PaymentTypeTO paymentType);

    /**
     * PROC:01 Initiates a pain payment. Called by the channel layer.
     * <p>
     * This call sets the status RCVD
     *
     * @param scaInfoTO   : SCA information
     * @param payment     : the xml payment object
     * @param paymentType : the payment type
     * @return : the sca response object.
     */
    String initiatePainPayment(ScaInfoTO scaInfoTO, String payment, PaymentTypeTO paymentType);

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
    Object getPaymentById(String paymentId);

    /**
     * Checks the possibility of payment cancellation
     *
     * @param scaInfoTO : SCA information
     * @param paymentId : the payment id
     * @return : the auth response object.
     */
    SCAPaymentResponseTO initiatePaymentCancellation(ScaInfoTO scaInfoTO, String paymentId);

    String iban(String paymentId);


    SCAPaymentResponseTO loadSCAForPaymentData(ScaInfoTO scaInfoTO, String paymentId);

    SCAPaymentResponseTO selectSCAMethodForPayment(ScaInfoTO scaInfoTO, String paymentId);

    SCAPaymentResponseTO loadSCAForCancelPaymentData(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

    SCAPaymentResponseTO selectSCAMethodForCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

    SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId);

    List<PaymentTO> getPendingPeriodicPayments(ScaInfoTO scaInfoTO);
}

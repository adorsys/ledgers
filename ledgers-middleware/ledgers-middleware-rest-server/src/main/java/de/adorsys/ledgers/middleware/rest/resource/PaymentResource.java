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

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(PaymentRestAPI.BASE_PATH)
public class PaymentResource implements PaymentRestAPI {
    private final MiddlewarePaymentService paymentService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("hasAccessToAccountByPaymentId(#paymentId)")
    public ResponseEntity<TransactionStatusTO> getPaymentStatusById(String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatusById(paymentId));
    }

    @Override
    @PreAuthorize("hasAccessToAccountByPaymentId(#paymentId)")
    public ResponseEntity<PaymentTO> getPaymentById(String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentTO>> getPendingPeriodicPayments() {
        return ResponseEntity.ok(paymentService.getPendingPeriodicPayments(scaInfoHolder.getScaInfo()));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomPageImpl<PaymentTO>> getPendingPeriodicPaymentsPaged(int page, int size) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        return ResponseEntity.ok(paymentService.getPendingPeriodicPaymentsPaged(scaInfoHolder.getScaInfo(), pageable));
    }

    @Override
    @PreAuthorize("hasAccessToAccountWithIban(#payment.debtorAccount.iban)")
    public ResponseEntity<SCAPaymentResponseTO> initiatePayment(PaymentTypeTO paymentType, PaymentTO payment) {
        return new ResponseEntity<>(paymentService.initiatePayment(scaInfoHolder.getScaInfo(), payment, paymentType), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasPartialScope() and hasAccessToAccountByPaymentId(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> executePayment(String paymentId) {
        return ResponseEntity.accepted().body(paymentService.executePayment(scaInfoHolder.getScaInfo(), paymentId));
    }

    @Override
    @PreAuthorize("hasAccessToAccountByPaymentId(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(String paymentId) {
        return ResponseEntity.ok(paymentService.initiatePaymentCancellation(scaInfoHolder.getScaInfo(), paymentId));
    }

    @Override
    @PreAuthorize("hasPartialScope() and hasAccessToAccountByPaymentId(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> executeCancelPayment(String paymentId) {
        return ResponseEntity.ok(paymentService.authorizeCancelPayment(scaInfoHolder.getScaInfo(), paymentId));
    }
}

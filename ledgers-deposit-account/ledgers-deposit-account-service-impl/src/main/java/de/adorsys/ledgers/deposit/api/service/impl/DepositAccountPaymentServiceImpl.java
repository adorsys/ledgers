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

package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.*;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepositAccountPaymentServiceImpl implements DepositAccountPaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PostingService postingService;

    private final LedgerService ledgerService;

    private final DepositAccountConfigService depositAccountConfigService; //NOPMD //TODO remove after fixing

    public DepositAccountPaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper, PostingService postingService, LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.postingService = postingService;
        this.ledgerService = ledgerService;
        this.depositAccountConfigService = depositAccountConfigService;
    }

    @Override
    public PaymentResultBO<TransactionStatusBO> getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment
                                                      .map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return new PaymentResultBO<>(TransactionStatusBO.valueOf(transactionStatus.name()));
    }

    @Override
    public PaymentBO getPaymentById(PaymentTypeBO paymentType, PaymentProductBO paymentProduct, String paymentId) throws PaymentNotFoundException {
        PaymentBO payment = paymentRepository.findById(paymentId)
                                    .map(paymentMapper::toPaymentBO)
                                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return filterPaymentByTypeAndProduct(payment, paymentType, paymentProduct);
    }

    @Override
    public PaymentBO initiatePayment(PaymentBO payment) {
        Payment persistedPayment = paymentMapper.toPayment(payment);
        persistedPayment.getTargets().forEach(t -> t.setPayment(persistedPayment));
        Payment save = paymentRepository.save(persistedPayment);
        return paymentMapper.toPaymentBO(save);
    }

    @Override
    public List<TransactionDetailsBO> executePayment(String paymentId, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException, PaymentProcessingException {
        PaymentBO storedPayment = getPaymentById(paymentType, paymentProduct, paymentId);
        if (storedPayment.getTransactionStatus() != TransactionStatusBO.RCVD) {
            throw new PaymentProcessingException("Payment execution failed due to: " + storedPayment.getTransactionStatus() + ", payment id: " + paymentId);
        }

        String oprDetails;
        try {
            oprDetails = SerializationUtils.writeValueAsString(storedPayment); //TODO Consider overriding .toString()?
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }

        LedgerBO ledger = new LedgerBO(); //TODO @fpo  ->  depositAccountConfigService.getLedger();
        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount;
        try {
            debtorLedgerAccount = ledgerService.findLedgerAccount(ledger, storedPayment.getDebtorAccount().getIban());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        List<List<PostingLineBO>> lists = storedPayment.getTargets().stream()
                                                  .map(target -> preparePostingLines(ledger, debtorLedgerAccount, oprDetails, target)).collect(Collectors.toList());

        List<PostingBO> postings = preparePostings(storedPayment, oprDetails, ledger, lists);

        return executeTransactions(postings);
    }

    private List<TransactionDetailsBO> executeTransactions(List<PostingBO> postings) throws PaymentProcessingException {
        List<TransactionDetailsBO> responses = new ArrayList<>();
        for (PostingBO posting : postings) {
            try {
                posting = postingService.newPosting(posting);
            } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException | DoubleEntryAccountingException e) {
                throw new PaymentProcessingException(e.getMessage());
            }
            responses.add(paymentMapper.toTransaction(posting));
        }
        return responses;
    }

    private List<PostingBO> preparePostings(PaymentBO storedPayment, String oprDetails, LedgerBO ledger, List<List<PostingLineBO>> lists) {
        if (storedPayment.getPaymentType() == PaymentTypeBO.BULK && storedPayment.getBatchBookingPreferred()) {
            List<PostingLineBO> lines = lists.stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toList());
            return Collections.singletonList(buildPosting(oprDetails, ledger, lines));
        } else {
            return lists.stream()
                           .map(lines -> buildPosting(oprDetails, ledger, lines))
                           .collect(Collectors.toList());
        }
    }

    private List<PostingLineBO> preparePostingLines(LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, String oprDetails, PaymentTargetBO paymentTarget) {
        LedgerAccountBO creditLedgerAccount;

        try {
            creditLedgerAccount = ledgerService.findLedgerAccount(ledger, paymentTarget.getCreditorAccount().getIban());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            creditLedgerAccount = new LedgerAccountBO(); //TODO @fpo -> depositAccountConfigService.getClearingAccount();
        }

        BigDecimal amount = paymentTarget.getInstructedAmount().getAmount();
        PostingLineBO debitLine = buildDebitLine(oprDetails, debtorLedgerAccount, amount);
        PostingLineBO creditLine = buildCreditLine(oprDetails, creditLedgerAccount, amount);
        return Arrays.asList(debitLine, creditLine);
    }

    private PaymentBO filterPaymentByTypeAndProduct(PaymentBO payment, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException {
        boolean isPresentPayment = PaymentTypeBO.valueOf(payment.getPaymentType().name()) == paymentType;
        if (isPresentPayment && payment.getPaymentType() != PaymentTypeBO.BULK) {
            isPresentPayment = payment.getTargets().stream()
                                       .map(t -> PaymentProductBO.valueOf(t.getPaymentProduct().name()))
                                       .allMatch(t -> t == paymentProduct);
        }
        if (!isPresentPayment) {
            throw new PaymentNotFoundException(payment.getPaymentId());
        }
        return payment;
    }

    private PostingLineBO buildCreditLine(String oprDetails, LedgerAccountBO creditLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, creditLedgerAccount, BigDecimal.ZERO, amount);
    }

    private PostingLineBO buildDebitLine(String oprDetails, LedgerAccountBO debtorLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, debtorLedgerAccount, amount, BigDecimal.ZERO);
    }

    private PostingLineBO buildPostingLine(String oprDetails, LedgerAccountBO creditLedgerAccount, BigDecimal debitAmount, BigDecimal creditAmount) {
        PostingLineBO p = new PostingLineBO();
        p.setDetails(oprDetails);
        p.setAccount(creditLedgerAccount);
        p.setDebitAmount(debitAmount);
        p.setCreditAmount(creditAmount);
        return p;
    }

    private PostingBO buildPosting(String oprDetails, LedgerBO ledger, List<PostingLineBO> lines) {
        LocalDateTime now = LocalDateTime.now();
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setOprTime(now);
        p.setOprDetails(oprDetails);
        p.setPstTime(now);
        p.setPstType(PostingTypeBO.BUSI_TX);
        p.setPstStatus(PostingStatusBO.POSTED);
        p.setLedger(ledger);
        p.setValTime(now);
        p.setLines(lines);
        return p;
    }
}

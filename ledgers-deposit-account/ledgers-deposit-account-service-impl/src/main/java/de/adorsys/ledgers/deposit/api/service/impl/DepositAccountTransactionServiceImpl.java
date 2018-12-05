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
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.*;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepositAccountTransactionServiceImpl extends AbstractServiceImpl implements DepositAccountTransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepositAccountServiceImpl.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PostingService postingService;

    private final TransactionDetailsMapper transactionDetailsMapper;

    public DepositAccountTransactionServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper,
                                                PostingService postingService, LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService,
                                                TransactionDetailsMapper transactionDetailsMapper) {
        super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.postingService = postingService;
        this.transactionDetailsMapper = transactionDetailsMapper;
    }

    /**
     * Execute a payment. This principally applies to:
     * - Single payment
     * - Future date payment
     * - Periodic Payment
     * - Bulk Payment with batch execution
     * <p>
     * + Bulk payment without batch execution will be split into single payments
     * and each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO bookPayment(String paymentId, LocalDateTime pstTime) throws PaymentNotFoundException {
        Payment payment = paymentRepository.findById(paymentId)
                                  .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        PaymentBO storedPayment = paymentMapper.toPaymentBO(payment);

        // We do not need to store the whole payment in the details. We will keep a Map<String, String> here for simplicity.
        String oprDetails = serializeOprDetails(paymentMapper.toPaymentOrder(storedPayment));
        LedgerBO ledger = loadLedger();

        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount = getDebtorAccount(ledger, storedPayment.getDebtorAccount().getIban());

        Set<PostingBO> postings = storedPayment.getPaymentType() == PaymentTypeBO.BULK && storedPayment.getBatchBookingPreferred()
                                          ? createBatchPostings(pstTime, oprDetails, ledger, debtorLedgerAccount, storedPayment)
                                          : createRegularPostings(pstTime, oprDetails, ledger, debtorLedgerAccount, storedPayment);

        postings.forEach(this::executeTransactions);
        payment.setTransactionStatus(TransactionStatus.ACSP);
        payment = paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(payment.getTransactionStatus().name());
    }

    private Set<PostingBO> createRegularPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentBO storedPayment) {
        return storedPayment.getTargets().stream()
                       .map(t -> {
                           t.setPayment(storedPayment);
                           return buildDCPosting(pstTime, oprDetails, ledger, debtorLedgerAccount, t);
                       })
                       .collect(Collectors.toSet());
    }

    private Set<PostingBO> createBatchPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentBO storedPayment) {
        PostingBO posting = buildPosting(pstTime, storedPayment.getPaymentId(), oprDetails, ledger);
        List<PostingLineBO> creditLines = storedPayment.getTargets().stream()
                                                  .map(t -> {
                                                      t.setPayment(storedPayment);
                                                      return buildCreditLine(t, ledger, pstTime.toLocalDate());
                                                  })
                                                  .collect(Collectors.toList());

        AmountBO amount = calculateDebitAmountBatch(storedPayment, creditLines);
        String id = UUID.randomUUID().toString();
        String debitLineDetails = serializeOprDetails(paymentMapper.toPaymentTargetDetailsBatch(id, storedPayment, amount, pstTime.toLocalDate()));
        PostingLineBO debitLine = buildPostingLine(debitLineDetails, debtorLedgerAccount, amount.getAmount(), BigDecimal.ZERO, storedPayment.getPaymentId(), id);
        posting.getLines().add(debitLine);
        posting.getLines().addAll(creditLines);

        return new HashSet<>(Collections.singletonList(posting));
    }

    private PostingBO buildDCPosting(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentTargetBO target) {
        PostingBO posting = buildPosting(pstTime, target.getPayment().getPaymentId(), oprDetails, ledger);
        String id = UUID.randomUUID().toString();
        String targetDetails = serializeOprDetails(paymentMapper.toPaymentTargetDetails(id, target, pstTime.toLocalDate()));
        PostingLineBO debitLine = buildPostingLine(targetDetails, debtorLedgerAccount, target.getInstructedAmount().getAmount(), BigDecimal.ZERO, posting.getOprId(), id);
        PostingLineBO creditLine = buildCreditLine(target, ledger, pstTime.toLocalDate());
        posting.getLines().addAll(Arrays.asList(debitLine, creditLine));
        return posting;
    }

    private AmountBO calculateDebitAmountBatch(PaymentBO storedPayment, List<PostingLineBO> creditLines) {
        Currency currency = storedPayment.getTargets().iterator().next().getInstructedAmount().getCurrency();
        BigDecimal debitAmount = creditLines.stream()
                                         .map(PostingLineBO::getCreditAmount)
                                         .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AmountBO(currency, debitAmount);
    }

    private PostingLineBO buildCreditLine(PaymentTargetBO target, LedgerBO ledger, LocalDate pstTime) {
        LedgerAccountBO creditorAccount = getCreditorAccount(ledger, target.getCreditorAccount().getIban(), target.getPaymentProduct());
        String id = UUID.randomUUID().toString();
        String targetDetails = serializeOprDetails(paymentMapper.toPaymentTargetDetails(id, target, pstTime));
        return buildPostingLine(targetDetails, creditorAccount, BigDecimal.ZERO, target.getInstructedAmount().getAmount(), target.getPaymentId(), id);
    }

    private LedgerAccountBO getDebtorAccount(LedgerBO ledger, String iban) {
        try {
            return ledgerService.findLedgerAccount(ledger, iban);
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }
    }

    private LedgerAccountBO getCreditorAccount(LedgerBO ledger, String iban, PaymentProductBO paymentProduct) {
        try {
            return ledgerService.findLedgerAccount(ledger, iban);
        } catch (LedgerNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new PaymentProcessingException(e.getMessage(), e);
        } catch (LedgerAccountNotFoundException ex) {
            return loadClearingAccount(ledger, paymentProduct);
        }
    }

    private List<TransactionDetailsBO> executeTransactions(PostingBO posting) throws PaymentProcessingException {
        try {
            PostingBO p = postingService.newPosting(posting);
            return p.getLines().stream()
                           .map(transactionDetailsMapper::toTransaction)
                           .collect(Collectors.toList());
        } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException | DoubleEntryAccountingException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
    }

    private PostingLineBO buildPostingLine(String lineDetails, LedgerAccountBO ledgerAccount, BigDecimal debitAmount, BigDecimal creditAmount, String subOprSrcId, String lineId) {
        PostingLineBO line = new PostingLineBO();
        line.setId(lineId);
        line.setDetails(lineDetails);
        line.setAccount(ledgerAccount);
        line.setDebitAmount(debitAmount);
        line.setCreditAmount(creditAmount);
        line.setSubOprSrcId(subOprSrcId);
        return line;
    }

    private PostingBO buildPosting(LocalDateTime pstTime, String paymentId, String oprDetails, LedgerBO ledger) {
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setOprTime(pstTime);
        p.setOprSrc(paymentId);
        p.setOprDetails(oprDetails);
        p.setPstTime(pstTime);
        p.setPstType(PostingTypeBO.BUSI_TX);
        p.setPstStatus(PostingStatusBO.POSTED);
        p.setLedger(ledger);
        p.setValTime(pstTime);
        return p;
    }

    private <T> String serializeOprDetails(T orderDetails) throws PaymentProcessingException {
        try {
            return SerializationUtils.writeValueAsString(orderDetails);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }
    }
}
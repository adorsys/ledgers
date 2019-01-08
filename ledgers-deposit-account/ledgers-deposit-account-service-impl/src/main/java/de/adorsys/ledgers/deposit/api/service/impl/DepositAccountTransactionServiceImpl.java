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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.domain.PostingStatusBO;
import de.adorsys.ledgers.postings.api.domain.PostingTypeBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;

@Service
public class DepositAccountTransactionServiceImpl extends AbstractServiceImpl implements DepositAccountTransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepositAccountServiceImpl.class);

    private final PaymentMapper paymentMapper;
    private final PostingService postingService;

    public DepositAccountTransactionServiceImpl(PaymentMapper paymentMapper,
                                                PostingService postingService, LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService) {
        super(depositAccountConfigService, ledgerService);
        this.paymentMapper = paymentMapper;
        this.postingService = postingService;
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
    public void bookPayment(Payment payment, LocalDateTime pstTime, String userName) {
        PaymentBO paymentObj = paymentMapper.toPaymentBO(payment);

        // We do not need to store the whole payment in the details. We will keep a Map<String, String> here for simplicity.
        String oprDetails = serializeOprDetails(paymentMapper.toPaymentOrder(paymentObj));
        LedgerBO ledger = loadLedger();

        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount = getDebtorAccount(ledger, paymentObj.getDebtorAccount().getIban());

        Set<PostingBO> postings = paymentObj.getPaymentType() == PaymentTypeBO.BULK && paymentObj.getBatchBookingPreferred()
                                          ? createBatchPostings(pstTime, oprDetails, ledger, debtorLedgerAccount, paymentObj, userName)
                                          : createRegularPostings(pstTime, oprDetails, ledger, debtorLedgerAccount, paymentObj, userName);

        postings.forEach(this::executeTransactions);
    }

    private Set<PostingBO> createRegularPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentBO payment, String userName) {
        return payment.getTargets().stream()
                       .map(t -> {
                           t.setPayment(payment);
                           return buildDCPosting(pstTime, oprDetails, ledger, debtorLedgerAccount, t, userName);
                       })
                       .collect(Collectors.toSet());
    }

    private Set<PostingBO> createBatchPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentBO payment, String userName) {
        PostingBO posting = buildPosting(pstTime, payment.getPaymentId(), oprDetails, ledger, userName);
        List<PostingLineBO> creditLines = payment.getTargets().stream()
                                                  .map(t -> {
                                                      t.setPayment(payment);
                                                      return buildCreditLine(t, ledger, pstTime.toLocalDate());
                                                  })
                                                  .collect(Collectors.toList());

        AmountBO amount = calculateDebitAmountBatch(payment, creditLines);
        String id = Ids.id();
        String debitLineDetails = serializeOprDetails(paymentMapper.toPaymentTargetDetailsBatch(id, payment, amount, pstTime.toLocalDate()));
        PostingLineBO debitLine = buildPostingLine(debitLineDetails, debtorLedgerAccount, amount.getAmount(), BigDecimal.ZERO, payment.getPaymentId(), id);
        posting.getLines().add(debitLine);
        posting.getLines().addAll(creditLines);

        return new HashSet<>(Collections.singletonList(posting));
    }

    private PostingBO buildDCPosting(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount, PaymentTargetBO target, String userName) {
        PostingBO posting = buildPosting(pstTime, target.getPayment().getPaymentId(), oprDetails, ledger, userName);
        String id = Ids.id();
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
        String id = Ids.id();
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

    private void executeTransactions(PostingBO posting) throws PaymentProcessingException {
        try {
            postingService.newPosting(posting);
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

    private PostingBO buildPosting(LocalDateTime pstTime, String paymentId, String oprDetails, LedgerBO ledger, String userName) {
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setRecordUser(userName);
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
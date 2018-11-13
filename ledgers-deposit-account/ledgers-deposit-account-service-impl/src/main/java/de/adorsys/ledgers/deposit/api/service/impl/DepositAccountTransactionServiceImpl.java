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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
     * + Bulk payment without batch execution will be splited into single payments
     * and each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO bookPayment(String paymentId, LocalDateTime pstTime) throws PaymentNotFoundException, PaymentProcessingException {

        Payment payment = paymentRepository.findById(paymentId)
                                  .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        PaymentBO storedPayment = paymentMapper.toPaymentBO(payment);

        // We do not need to store the whole payment in the details. We will keep a Map<String, String> here for simplicity.
        PaymentOrderDetailsBO po = new PaymentOrderDetailsBO(storedPayment);
        String oprDetails;
        try {
            oprDetails = SerializationUtils.writeValueAsString(po);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }

        LedgerBO ledger = loadLedger();

        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount;
        try {
            debtorLedgerAccount = ledgerService.findLedgerAccount(ledger, storedPayment.getDebtorAccount().getIban());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        PostingBO posting = buildPosting(pstTime, po, oprDetails, ledger);
        // Initialize the debit line with zero.
        PostingLineBO debitLine = buildDebitLine(posting, oprDetails, debtorLedgerAccount, BigDecimal.ZERO);

        Set<PostingBO> postings = storedPayment.getTargets().stream().map(target -> {
            target.setPayment(storedPayment);
            PostingBO postingBO = posting;
            PostingLineBO debitLineBO = debitLine;
            // No Batch booking. Then each target shall finish in a proper booking.
            if (storedPayment.getPaymentType() == PaymentTypeBO.BULK && !storedPayment.getBatchBookingPreferred()) {
                postingBO = buildPosting(pstTime, po, oprDetails, ledger);
                // Initialize the debit line with zero.
                debitLineBO = buildDebitLine(posting, oprDetails, debtorLedgerAccount, BigDecimal.ZERO);
            }
            preparePostingLines(postingBO, ledger, debitLineBO, target);
            return postingBO;
        }).collect(Collectors.toSet());

        fixDebitLine(storedPayment, posting, debitLine);

        postings.forEach(this::executeTransactions);
        payment.setTransactionStatus(TransactionStatus.ACSP);
        payment = paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(payment.getTransactionStatus().name());
    }

    private void fixDebitLine(PaymentBO storedPayment, PostingBO posting, PostingLineBO debitLine) {
        // For bulk payment with batch booking, we need another debit line.
        if (storedPayment.getPaymentType() == PaymentTypeBO.BULK && storedPayment.getBatchBookingPreferred()) {
            PaymentTargetBO paymentTarget = storedPayment.getTargets().iterator().next();
            // Isolate payment
            PaymentTargetDetailsBO tx = mapPaymentTargetDetailsBO(storedPayment, posting, debitLine, paymentTarget);

            String pmtDetails;
            try {
                pmtDetails = SerializationUtils.writeValueAsString(tx);
            } catch (JsonProcessingException e) {
                throw new PaymentProcessingException("Payment object can't be serialized", e);
            }
            debitLine.setDetails(pmtDetails);
        }
    }

    private PaymentTargetDetailsBO mapPaymentTargetDetailsBO(PaymentBO storedPayment, PostingBO posting, PostingLineBO debitLine, PaymentTargetBO paymentTarget) {
        PaymentTargetDetailsBO tx = new PaymentTargetDetailsBO();
        tx.setTransactionId(storedPayment.getPaymentId());
        tx.setEndToEndId(paymentTarget.getEndToEndIdentification());
        tx.setBookingDate(posting.getPstTime().toLocalDate());
        tx.setValueDate(posting.getPstTime().toLocalDate());
        AmountBO txAmount = new AmountBO();
        txAmount.setAmount(debitLine.getDebitAmount());
        txAmount.setCurrency(paymentTarget.getInstructedAmount().getCurrency());
        tx.setTransactionAmount(txAmount);
        tx.setDebtorAccount(storedPayment.getDebtorAccount());
        tx.setRemittanceInformationUnstructured(paymentTarget.getRemittanceInformationUnstructured());
        tx.setPaymentOrderId(storedPayment.getPaymentId());
        tx.setPaymentType(storedPayment.getPaymentType());
        tx.setPaymentProduct(paymentTarget.getPaymentProduct());
      /*tx.setCreditorName(); //TODO @fpo why it is commented? Should it be uncommented after or simply removed?
        tx.setCreditorAccount();
        tx.setCreditorAddress();
        tx.setCreditorAgent();*/
        return tx;
    }

    private List<TransactionDetailsBO> executeTransactions(PostingBO posting) throws PaymentProcessingException {
        try {
            PostingBO p = postingService.newPosting(posting); //TODO Here in BulkPayment with batchPref: false we get only one Postingline and our balance is getting spoiled
            return p.getLines().stream()
                           .map(transactionDetailsMapper::toTransaction)
                           .collect(Collectors.toList());
        } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException | DoubleEntryAccountingException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
    }

    private void preparePostingLines(PostingBO posting, LedgerBO ledger, final PostingLineBO debitLine, PaymentTargetBO paymentTarget) {
        LedgerAccountBO creditLedgerAccount;

        try {
            creditLedgerAccount = ledgerService.findLedgerAccount(ledger, paymentTarget.getCreditorAccount().getIban());
        } catch (LedgerNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new PaymentProcessingException(e.getMessage(), e);
        } catch (LedgerAccountNotFoundException e1) {
            creditLedgerAccount = loadClearingAccount(ledger, paymentTarget.getPaymentProduct());
        }

        // Isolate payment
        PaymentTargetDetailsBO tx = new PaymentTargetDetailsBO();
        tx.setTransactionId(paymentTarget.getPaymentId());
        tx.setEndToEndId(paymentTarget.getEndToEndIdentification());
        tx.setBookingDate(posting.getPstTime().toLocalDate());
        tx.setValueDate(posting.getPstTime().toLocalDate());
        tx.setTransactionAmount(paymentTarget.getInstructedAmount());
        tx.setCreditorName(paymentTarget.getCreditorName());
        tx.setCreditorAccount(paymentTarget.getCreditorAccount());
        tx.setDebtorAccount(paymentTarget.getPayment().getDebtorAccount());
        tx.setRemittanceInformationUnstructured(paymentTarget.getRemittanceInformationUnstructured());
        tx.setCreditorAddress(paymentTarget.getCreditorAddress());
        tx.setPaymentOrderId(paymentTarget.getPayment().getPaymentId());
        tx.setPaymentType(paymentTarget.getPayment().getPaymentType());
        tx.setPaymentProduct(paymentTarget.getPaymentProduct());
        tx.setCreditorAgent(paymentTarget.getCreditorAgent());

        String pymtDetails;
        try {
            pymtDetails = SerializationUtils.writeValueAsString(tx);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }

        BigDecimal amount = paymentTarget.getInstructedAmount().getAmount();
        debitLine.setDebitAmount(debitLine.getDebitAmount().add(amount));
        // Set payment details on the debit line
        debitLine.setDetails(pymtDetails);
        buildCreditLine(posting, pymtDetails, creditLedgerAccount, amount, paymentTarget);
    }

    private PostingLineBO buildCreditLine(final PostingBO posting, String oprDetails, LedgerAccountBO creditLedgerAccount, BigDecimal amount, PaymentTargetBO paymentTarget) {
        PostingLineBO line = buildPostingLine(posting, oprDetails, creditLedgerAccount, BigDecimal.ZERO, amount);
        line.setSubOprSrcId(paymentTarget.getPaymentId());
        return line;
    }

    private PostingLineBO buildDebitLine(final PostingBO posting, String oprDetails, LedgerAccountBO debtorLedgerAccount, BigDecimal amount) {
        return buildPostingLine(posting, oprDetails, debtorLedgerAccount, amount, BigDecimal.ZERO);
    }

    private PostingLineBO buildPostingLine(final PostingBO posting, String lineDetails, LedgerAccountBO ledgerAccount, BigDecimal debitAmount, BigDecimal creditAmount) {
        PostingLineBO p = new PostingLineBO();
        p.setDetails(lineDetails);
        p.setAccount(ledgerAccount); //TODO Refactor whole shit
        p.setDebitAmount(debitAmount);
        p.setCreditAmount(creditAmount);
        posting.getLines().add(p);
        return p;
    }

    private PostingBO buildPosting(LocalDateTime pstTime, PaymentOrderDetailsBO po, String oprDetails, LedgerBO ledger) {
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setOprTime(pstTime);
        p.setOprSrc(po.getPaymentId());
        p.setOprDetails(oprDetails);
        p.setPstTime(pstTime);
        p.setPstType(PostingTypeBO.BUSI_TX);
        p.setPstStatus(PostingStatusBO.POSTED);
        p.setLedger(ledger);
        p.setValTime(pstTime);
        return p;
    }
}

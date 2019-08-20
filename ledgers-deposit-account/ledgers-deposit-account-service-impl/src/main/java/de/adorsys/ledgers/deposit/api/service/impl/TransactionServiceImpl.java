package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.TransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.SerializeService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.SINGLE;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSC;
import static de.adorsys.ledgers.postings.api.domain.PostingStatusBO.POSTED;
import static de.adorsys.ledgers.postings.api.domain.PostingTypeBO.BUSI_TX;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final String MOCK_DATA_IMPORT = "MockDataImport";
    public static final String SEPA_CLEARING_ACCOUNT = "11031";
    private final SerializeService serializeService;
    private final PostingService postingService;
    private final LedgerService ledgerService;
    private final DepositAccountConfigService depositAccountConfigService;

    @Override
    public Map<String, String> bookMockTransaction(List<MockBookingDetails> trDetails) {
        Map<String, String> errorMap = new HashMap<>();
        trDetails.forEach(d -> {
            try {
                performTransaction(d);
            } catch (Exception e) {
                errorMap.put(d.toString(), e.getMessage());
            }
        });
        return errorMap;
    }

    private void performTransaction(MockBookingDetails details) {
        LedgerBO ledger = loadLedger();

        if (details.isPaymentTransaction()) {
            performPaymentTransaction(ledger, details);
        } else {
            performDepositTransaction(ledger, details);
        }
    }

    private LedgerBO loadLedger() {
        String ledgerName = depositAccountConfigService.getLedger();
        return ledgerService.findLedgerByName(ledgerName)
                       .orElseThrow(() -> new IllegalStateException(String.format("Ledger with name %s not found", ledgerName)));
    }

    private void performDepositTransaction(LedgerBO ledger, MockBookingDetails details) {
        AccountReferenceBO debtorAccount = getAccountReference(details.getOtherAccount(), details);

        PostingBO posting = composePosting(ledger, details, debtorAccount);
        PostingLineBO debitLine = composeLine(posting, details, ledger, false, true);
        PostingLineBO creditLine = composeLine(posting, details, ledger, false, false);
        posting.setLines(Arrays.asList(debitLine, creditLine));
        postingService.newPosting(posting);
    }

    private void performPaymentTransaction(LedgerBO ledger, MockBookingDetails details) {
        AccountReferenceBO debtorAccount = getAccountReference(details.getUserAccount(), details);

        PostingBO posting = composePosting(ledger, details, debtorAccount);
        PostingLineBO debitLine = composeLine(posting, details, ledger, true, true);
        PostingLineBO creditLine = composeLine(posting, details, ledger, true, false);
        posting.setLines(Arrays.asList(debitLine, creditLine));
        postingService.newPosting(posting);
    }

    private PostingLineBO composeLine(PostingBO posting, MockBookingDetails details, LedgerBO ledger, boolean isPayment, boolean isDebitLine) {
        PostingLineBO line = new PostingLineBO();
        line.setId(Ids.id());
        line.setAccount(isDebitLine
                                ? resolveAccountForDebitLine(details, ledger, isPayment)
                                : resolveAccountForCreditLine(details, ledger, isPayment));
        line.setDebitAmount(isDebitLine
                                    ? resolveAmountByOperationType(details, isPayment)
                                    : BigDecimal.ZERO);
        line.setCreditAmount(isDebitLine
                                     ? BigDecimal.ZERO
                                     : resolveAmountByOperationType(details, isPayment));
        return fillCommonPostingLineFields(posting, details, line);
    }


    private BigDecimal resolveAmountByOperationType(MockBookingDetails details, boolean isPayment) {
        return isPayment
                       ? details.getAmount().negate()
                       : details.getAmount();
    }

    private LedgerAccountBO resolveAccountForDebitLine(MockBookingDetails details, LedgerBO ledger, boolean isPayment) {
        return isPayment
                       ? ledgerService.findLedgerAccount(ledger, details.getUserAccount())
                       : checkOtherAccountOrLoadClearing(details, ledger);
    }

    private LedgerAccountBO resolveAccountForCreditLine(MockBookingDetails details, LedgerBO ledger, boolean isPayment) {
        return isPayment
                       ? checkOtherAccountOrLoadClearing(details, ledger)
                       : ledgerService.findLedgerAccount(ledger, details.getUserAccount());
    }

    private LedgerAccountBO checkOtherAccountOrLoadClearing(MockBookingDetails details, LedgerBO ledger) {
        return ledgerService.checkIfLedgerAccountExist(ledger, details.getOtherAccount())
                       ? ledgerService.findLedgerAccount(ledger, details.getOtherAccount())
                       : ledgerService.findLedgerAccount(ledger, SEPA_CLEARING_ACCOUNT);
    }

    private PostingLineBO fillCommonPostingLineFields(PostingBO posting, MockBookingDetails details, PostingLineBO line) {
        String lineDetails = createPostingLineDetails(details, line.getId());
        line.setDetails(lineDetails);
        line.setSubOprSrcId(posting.getOprId());
        line.setOprId(posting.getOprId()); //TODO no payment target here so just set oprId once again
        line.setPstTime(posting.getPstTime());
        line.setPstType(BUSI_TX);
        line.setPstStatus(POSTED);
        return line;
    }

    private PostingBO composePosting(LedgerBO ledger, MockBookingDetails details, AccountReferenceBO debtorAccount) {
        PostingBO posting = new PostingBO();
        String postingDetails = createPostingDetails(details, debtorAccount);

        posting.setOprId(Ids.id());
        posting.setOprTime(LocalDateTime.now());
        posting.setOprDetails(postingDetails);
        posting.setOprType(MOCK_DATA_IMPORT);
        posting.setOprSrc(MOCK_DATA_IMPORT);

        posting.setRecordUser(details.getCrDrName()); //TODO to be discussed not sure it is appropriate
        posting.setPstTime(details.getBookingDate().atTime(LocalTime.now()));
        posting.setPstType(BUSI_TX);
        posting.setPstStatus(POSTED);

        posting.setLedger(ledger);
        posting.setValTime(details.getValueDate().atTime(LocalTime.now()));
        return posting;
    }


    private String createPostingDetails(MockBookingDetails details, AccountReferenceBO account) {
        return serializeService.serializeOprDetails(
                new PaymentOrderDetailsBO(null, false, details.getBookingDate(),
                        LocalTime.now(), SINGLE, null, null, null, null,
                        null, account, ACSC));
    }

    private String createPostingLineDetails(MockBookingDetails details, String lineId) {
        TransactionDetailsBO lineDetails = new TransactionDetailsBO();
        lineDetails.setTransactionId(lineId);
        lineDetails.setEndToEndId("MockedTransaction");
        lineDetails.setBookingDate(details.getBookingDate());
        lineDetails.setValueDate(details.getValueDate());
        lineDetails.setTransactionAmount(new AmountBO(details.getCurrency(), details.isPaymentTransaction()
                                                                                     ? details.getAmount().negate()
                                                                                     : details.getAmount()));
        lineDetails.setCreditorName(details.getCrDrName());
        AccountReferenceBO creditor = getCrDrReference(details, details.isPaymentTransaction());
        lineDetails.setCreditorAccount(creditor);
        AccountReferenceBO debtor = getCrDrReference(details, !details.isPaymentTransaction());
        lineDetails.setDebtorAccount(debtor);
        lineDetails.setRemittanceInformationUnstructured(details.getRemittance());
        return serializeService.serializeOprDetails(lineDetails);
    }

    private AccountReferenceBO getCrDrReference(MockBookingDetails details, boolean isPaymentOperation) {
        return getAccountReference(isPaymentOperation
                                           ? details.getUserAccount()
                                           : details.getOtherAccount(), details);
    }

    private AccountReferenceBO getAccountReference(String iban, MockBookingDetails details) {
        AccountReferenceBO reference = new AccountReferenceBO();
        reference.setCurrency(details.getCurrency());
        reference.setIban(iban);
        return reference;
    }
}

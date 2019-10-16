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
import de.adorsys.ledgers.postings.api.service.PostingMockService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.SINGLE;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSC;
import static de.adorsys.ledgers.postings.api.domain.PostingStatusBO.POSTED;
import static de.adorsys.ledgers.postings.api.domain.PostingTypeBO.BUSI_TX;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final String MOCK_DATA_IMPORT = "MockDataImport";
    private static final String SEPA_CLEARING_ACCOUNT = "11031";
    private static final int NANO_TO_SECOND = 1000000000;
    private final SerializeService serializeService;
    private final PostingMockService postingService;
    private final LedgerService ledgerService;
    private final DepositAccountConfigService depositAccountConfigService;

    @Override
    public Map<String, String> bookMockTransaction(List<MockBookingDetailsBO> trDetails) {
        log.info("Start upload mock transactions, size: {}", trDetails.size());
        long start = System.nanoTime();
        LedgerBO ledger = loadLedger();
        log.info("Loaded Ledger in {} seconds from start", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        Map<String, LedgerAccountBO> accounts = getAccounts(trDetails, ledger);
        log.info("Loaded Accounts in {} seconds from start", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        List<PostingBO> postings = new ArrayList<>();
        Map<String, String> errorMap = new HashMap<>();
        trDetails.forEach(d -> {
            try {
                postings.add(preparePosting(d, ledger, accounts));
            } catch (Exception e) {
                errorMap.put(d.toString(), e.getMessage());
            }
        });
        log.info("Populated postings in {} seconds from start", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        postingService.addPostingsAsBatch(postings);
        log.info("Initiation completed in {} seconds, errors: {}, av: {} seconds/transaction", (double) (System.nanoTime() - start) / NANO_TO_SECOND, errorMap.size(), ((double) (System.nanoTime() - start) / NANO_TO_SECOND) / trDetails.size());
        return errorMap;
    }

    private Map<String, LedgerAccountBO> getAccounts(List<MockBookingDetailsBO> transactions, LedgerBO ledger) {
        long start = System.nanoTime();
        Set<String> ibans = new HashSet<>();
        transactions.forEach(t -> {
            ibans.add(t.getUserAccount());
            ibans.add(t.getOtherAccount());
        });
        ibans.add(SEPA_CLEARING_ACCOUNT);
        Map<String, LedgerAccountBO> ledgerAccounts = ledgerService.finLedgerAccountsByIbans(ibans, ledger);

        log.info("Selected {} accounts in {}", ledgerAccounts.size(), (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        return ledgerAccounts;
    }

    private PostingBO preparePosting(MockBookingDetailsBO details, LedgerBO ledger, Map<String, LedgerAccountBO> accounts) {
        return details.isPaymentTransaction()
                       ? preparePaymentPosting(ledger, details, accounts)
                       : prepareDepositPosting(ledger, details, accounts);

    }

    private LedgerBO loadLedger() {
        String ledgerName = depositAccountConfigService.getLedger();
        return ledgerService.findLedgerByName(ledgerName)
                       .orElseThrow(() -> new IllegalStateException(String.format("Ledger with name %s not found", ledgerName)));
    }

    private PostingBO prepareDepositPosting(LedgerBO ledger, MockBookingDetailsBO details, Map<String, LedgerAccountBO> accounts) {
        AccountReferenceBO debtorAccount = getAccountReference(details.getOtherAccount(), details);

        PostingBO posting = composePosting(ledger, details, debtorAccount);
        PostingLineBO debitLine = composeLine(posting, details, false, true, accounts);
        PostingLineBO creditLine = composeLine(posting, details, false, false, accounts);
        posting.setLines(Arrays.asList(debitLine, creditLine));
        return posting;
    }

    private PostingBO preparePaymentPosting(LedgerBO ledger, MockBookingDetailsBO details, Map<String, LedgerAccountBO> accounts) {
        AccountReferenceBO debtorAccount = getAccountReference(details.getUserAccount(), details);

        PostingBO posting = composePosting(ledger, details, debtorAccount);
        PostingLineBO debitLine = composeLine(posting, details, true, true, accounts);
        PostingLineBO creditLine = composeLine(posting, details, true, false, accounts);
        posting.setLines(Arrays.asList(debitLine, creditLine));
        return posting;
    }

    private PostingLineBO composeLine(PostingBO posting, MockBookingDetailsBO details, boolean isPayment, boolean isDebitLine, Map<String, LedgerAccountBO> accounts) {
        PostingLineBO line = new PostingLineBO();
        line.setId(Ids.id());
        line.setAccount(isDebitLine
                                ? resolveAccountForDebitLine(details, isPayment, accounts)
                                : resolveAccountForCreditLine(details, isPayment, accounts));
        line.setDebitAmount(isDebitLine
                                    ? resolveAmountByOperationType(details, isPayment)
                                    : BigDecimal.ZERO);
        line.setCreditAmount(isDebitLine
                                     ? BigDecimal.ZERO
                                     : resolveAmountByOperationType(details, isPayment));
        return fillCommonPostingLineFields(posting, details, line);
    }


    private BigDecimal resolveAmountByOperationType(MockBookingDetailsBO details, boolean isPayment) {
        return isPayment
                       ? details.getAmount().negate()
                       : details.getAmount();
    }

    private LedgerAccountBO resolveAccountForDebitLine(MockBookingDetailsBO details, boolean isPayment, Map<String, LedgerAccountBO> accounts) {
        return isPayment
                       ? accounts.get(details.getUserAccount())
                       : checkOtherAccountOrLoadClearing(details, accounts);
    }

    private LedgerAccountBO resolveAccountForCreditLine(MockBookingDetailsBO details, boolean isPayment, Map<String, LedgerAccountBO> accounts) {
        return isPayment
                       ? checkOtherAccountOrLoadClearing(details, accounts)
                       : accounts.get(details.getUserAccount());
    }

    private LedgerAccountBO checkOtherAccountOrLoadClearing(MockBookingDetailsBO details, Map<String, LedgerAccountBO> accounts) {
        return Optional.ofNullable(accounts.get(details.getOtherAccount()))
                       .orElseGet(() -> accounts.get(SEPA_CLEARING_ACCOUNT));
    }

    private PostingLineBO fillCommonPostingLineFields(PostingBO posting, MockBookingDetailsBO details, PostingLineBO line) {
        String lineDetails = createPostingLineDetails(details, line.getId());
        line.setDetails(lineDetails);
        line.setSubOprSrcId(posting.getOprId());
        line.setOprId(posting.getOprId()); //TODO no payment target here so just set oprId once again
        line.setPstTime(posting.getPstTime());
        line.setPstType(BUSI_TX);
        line.setPstStatus(POSTED);
        return line;
    }

    private PostingBO composePosting(LedgerBO ledger, MockBookingDetailsBO details, AccountReferenceBO debtorAccount) {
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


    private String createPostingDetails(MockBookingDetailsBO details, AccountReferenceBO account) {
        return serializeService.serializeOprDetails(
                new PaymentOrderDetailsBO(null, false, details.getBookingDate(),
                        LocalTime.now(), SINGLE, null, null, null, null,
                        null, account, ACSC));
    }

    private String createPostingLineDetails(MockBookingDetailsBO details, String lineId) {
        TransactionDetailsBO lineDetails = new TransactionDetailsBO();
        lineDetails.setTransactionId(lineId);
        lineDetails.setEndToEndId(UUID.randomUUID().toString());
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

    private AccountReferenceBO getCrDrReference(MockBookingDetailsBO details, boolean isPaymentOperation) {
        return getAccountReference(isPaymentOperation
                                           ? details.getUserAccount()
                                           : details.getOtherAccount(), details);
    }

    private AccountReferenceBO getAccountReference(String iban, MockBookingDetailsBO details) {
        AccountReferenceBO reference = new AccountReferenceBO();
        reference.setCurrency(details.getCurrency());
        reference.setIban(iban);
        return reference;
    }
}

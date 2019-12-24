package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PostingMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.SerializeService;
import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.SINGLE;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountTransactionServiceImplTest {
    private static final String ACCOUNT_ID = "ACCOUNT_ID";
    private static final String POSTING_ID = "posting_ID";
    private static final String SYSTEM = "System";
    private static final LocalDateTime REQUEST_TIME = LocalDateTime.now();
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency CHF = Currency.getInstance("CHF");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final String IBAN = "DE1234567890";
    private static int PMT_ID = 0;

    @InjectMocks
    DepositAccountTransactionServiceImpl transactionService;
    @Mock
    private DepositAccountService depositAccountService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private PostingService postingService;
    @Mock
    private SerializeService serializeService;
    @Mock
    private PaymentMapper paymentMapper;
    private final PaymentMapper localPaymentMapper = Mappers.getMapper(PaymentMapper.class);
    @Mock
    private CurrencyExchangeRatesService exchangeRatesService;
    @Mock
    private PostingMapper postingMapper = Mappers.getMapper(PostingMapper.class);
    private final PostingMapper localPostingMapper = Mappers.getMapper(PostingMapper.class);

    private static final ObjectMapper STATIC_MAPPER;

    private DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);

    static {
        STATIC_MAPPER = new ObjectMapper()
                                .findAndRegisterModules()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                                .registerModule(new Jdk8Module())
                                .registerModule(new JavaTimeModule())
                                .registerModule(new ParameterNamesModule());
    }

    @Test(expected = DepositModuleException.class)
    public void depositCash_accountNotFound() {
        when(depositAccountService.getAccountDetailsById(anyString(), any(), anyBoolean())).thenThrow(DepositModuleException.class);
        transactionService.depositCash(ACCOUNT_ID, new AmountBO(EUR, BigDecimal.TEN), "recordUser");
    }

    @Test
    public void depositCash_OK() throws IOException {
        when(depositAccountService.getAccountDetailsById(anyString(), any(), anyBoolean())).thenReturn(getDepositAccountBO());
        when(depositAccountConfigService.getLedger()).thenReturn("mockbank");
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        when(depositAccountConfigService.getCashAccount()).thenReturn("id");
        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(new LedgerAccountBO());
        when(ledgerService.findLedgerAccountById(any())).thenReturn(new LedgerAccountBO());
        when(serializeService.serializeOprDetails(any())).thenAnswer(i -> STATIC_MAPPER.writeValueAsString(i.getArguments()[0]));
        when(paymentMapper.toDepositTransactionDetails(any(), any(), any(), anyString())).thenAnswer(i -> localPaymentMapper.toDepositTransactionDetails((AmountBO) i.getArguments()[0], (AccountReferenceBO) i.getArguments()[1], (LocalDate) i.getArguments()[2], (String) i.getArguments()[3]));
        when(postingMapper.buildPosting(any(), anyString(), anyString(), any(), anyString())).thenAnswer(i -> localPostingMapper.buildPosting((LocalDateTime) i.getArguments()[0], (String) i.getArguments()[1], (String) i.getArguments()[2], (LedgerBO) i.getArguments()[3], (String) i.getArguments()[4]));
        when(postingMapper.buildPostingLine(anyString(), any(), any(), any(), anyString(), anyString())).thenAnswer(i -> localPostingMapper.buildPostingLine((String) i.getArguments()[0], (LedgerAccountBO) i.getArguments()[1], (BigDecimal) i.getArguments()[2], (BigDecimal) i.getArguments()[3], (String) i.getArguments()[4], (String) i.getArguments()[5]));
        AmountBO amount = new AmountBO(EUR, BigDecimal.TEN);

        transactionService.depositCash(ACCOUNT_ID, amount, "recordUser");

        ArgumentCaptor<PostingBO> postingCaptor = ArgumentCaptor.forClass(PostingBO.class);
        verify(postingService, times(1)).newPosting(postingCaptor.capture());
        PostingBO posting = postingCaptor.getValue();
        assertThat(posting.getLines()).hasSize(2);
        for (PostingLineBO line : posting.getLines()) {
            assertThat(line.getId()).isNotBlank();
            TransactionDetailsBO transactionDetails = STATIC_MAPPER.readValue(line.getDetails(), TransactionDetailsBO.class);
            assertThat(transactionDetails.getEndToEndId()).isEqualTo(line.getId());
            assertThat(transactionDetails.getTransactionId()).isNotBlank();
            assertThat(transactionDetails.getBookingDate()).isEqualTo(transactionDetails.getValueDate());
            assertThat(transactionDetails.getCreditorAccount()).isEqualToComparingFieldByField(depositAccountMapper.toAccountReferenceBO(getDepositAccount()));
            assertThat(transactionDetails.getTransactionAmount()).isEqualToComparingFieldByField(amount);
        }
    }

    @Test
    public void bookPayment_single_same_currency() throws IOException {
        //given
        PaymentBO payment = getPayment(SINGLE, EUR, EUR, EUR, null);

        when(paymentMapper.toPaymentOrder(any())).thenAnswer(i -> localPaymentMapper.toPaymentOrder((PaymentBO) i.getArguments()[0]));
        when(paymentMapper.toPaymentTargetDetails(anyString(), any(), any(), any())).thenAnswer(i -> localPaymentMapper.toPaymentTargetDetails((String) i.getArguments()[0], (PaymentTargetBO) i.getArguments()[1], (LocalDate) i.getArguments()[2], (List<ExchangeRateBO>) i.getArguments()[3]));
        when(postingMapper.buildPosting(any(), anyString(), anyString(), any(), anyString())).thenAnswer(i -> localPostingMapper.buildPosting((LocalDateTime) i.getArguments()[0], (String) i.getArguments()[1], (String) i.getArguments()[2], (LedgerBO) i.getArguments()[3], (String) i.getArguments()[4]));
        when(postingMapper.buildPostingLine(anyString(), any(), any(), any(), anyString(), anyString())).thenAnswer(i -> localPostingMapper.buildPostingLine((String) i.getArguments()[0], (LedgerAccountBO) i.getArguments()[1], (BigDecimal) i.getArguments()[2], (BigDecimal) i.getArguments()[3], (String) i.getArguments()[4], (String) i.getArguments()[5]));
        when(serializeService.serializeOprDetails(any())).thenAnswer(i -> STATIC_MAPPER.writeValueAsString(i.getArguments()[0]));

        when(depositAccountConfigService.getLedger()).thenReturn("mockbank");
        when(ledgerService.findLedgerByName(anyString())).thenReturn(Optional.of(new LedgerBO("mockbank", "id", null, null, null, null, null)));

        when(exchangeRatesService.getExchangeRates(any(), any(), any())).thenReturn(getRates(EUR, EUR, EUR));

        when(depositAccountConfigService.getClearingAccount(any())).thenReturn("clearing");
        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(new LedgerAccountBO("clearing", new LedgerBO()));


        transactionService.bookPayment(payment, REQUEST_TIME, "TEST");
        ArgumentCaptor<PostingBO> postingCaptor = ArgumentCaptor.forClass(PostingBO.class);
        verify(postingService, times(1)).newPosting(postingCaptor.capture());

        PostingBO posting = postingCaptor.getValue();
        List<PostingLineBO> lines = posting.getLines();
        assertThat(lines.size()).isEqualTo(2);
        assertThat(dcAmountOk(lines)).isTrue();

        PostingLineBO line1 = lines.get(0);
        assertThat(line1).isEqualToIgnoringGivenFields(expectedLine(null, null, BigDecimal.TEN, BigDecimal.ZERO, null, null, null, posting.getOprId()), "id", "details");
        assertThat(line1.getDetails()).isNotBlank();
        assertThat(STATIC_MAPPER.readValue(line1.getDetails(), PaymentTargetDetailsBO.class)).isEqualToIgnoringGivenFields(getExpectedDetails(payment, line1.getId(), null));

        PostingLineBO line2 = lines.get(1);
        assertThat(line2).isEqualToIgnoringGivenFields(expectedLine(null, null, BigDecimal.ZERO, BigDecimal.TEN, null, null, null, payment.getPaymentId()), "id", "details");
        assertThat(line2.getDetails()).isNotBlank();
        assertThat(STATIC_MAPPER.readValue(line2.getDetails(), PaymentTargetDetailsBO.class)).isEqualToIgnoringGivenFields(getExpectedDetails(payment, line2.getId(), null));
    }

    //TODO ADD MORE TESTS HERE

    private PaymentTargetDetailsBO getExpectedDetails(PaymentBO payment, String trId, List<ExchangeRateBO> rates) {
        PaymentTargetDetailsBO details = new PaymentTargetDetailsBO();
        details.setTransactionStatus(null);
        details.setTransactionId(trId);
        details.setEndToEndId(payment.getTargets().get(0).getEndToEndIdentification());
        details.setPaymentProduct("sepa-credit-transfers");
        details.setBookingDate(REQUEST_TIME.toLocalDate());
        details.setValueDate(REQUEST_TIME.toLocalDate());
        details.setTransactionAmount(payment.getTargets().get(0).getInstructedAmount());
        details.setExchangeRate(rates);
        details.setCreditorName(payment.getTargets().get(0).getCreditorName());
        details.setCreditorAccount(payment.getTargets().get(0).getCreditorAccount());
        details.setCreditorAddress(payment.getTargets().get(0).getCreditorAddress());
        details.setDebtorName(null);
        details.setDebtorAccount(payment.getDebtorAccount());
        details.setPaymentOrderId(payment.getPaymentId());
        details.setPaymentType(payment.getPaymentType());
        return details;
    }

    private PostingLineBO expectedLine(String id, String account, BigDecimal debit, BigDecimal credit, String details, String oprId, String oprSrcId, String subOprSrcId) {
        PostingLineBO l = new PostingLineBO();
        l.setId(id);
        l.setAccount(new LedgerAccountBO(account, new LedgerBO()));
        l.setDebitAmount(debit);
        l.setCreditAmount(credit);
        l.setDetails(details);
        l.setOprId(oprId);
        l.setOprSrc(oprSrcId);
        l.setPstStatus(PostingStatusBO.POSTED);
        l.setPstTime(null);
        l.setPstType(null);
        l.setSubOprSrcId(subOprSrcId);
        return l;
    }

    private boolean dcAmountOk(List<PostingLineBO> lines) {
        Optional<BigDecimal> credit = lines.stream().map(PostingLineBO::getCreditAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> debit = lines.stream().map(PostingLineBO::getDebitAmount).reduce(BigDecimal::add);
        return credit.equals(debit);
    }

    private List<ExchangeRateBO> getRates(Currency debt, Currency amount, Currency cred) {
        if (debt.equals(amount) && amount.equals(cred)) {
            return Collections.emptyList();
        }
        List<ExchangeRateBO> ratesToReturn = new ArrayList<>();
        updateRatesList(amount, debt, ratesToReturn);
        updateRatesList(amount, cred, ratesToReturn);
        return ratesToReturn;
    }

    private void updateRatesList(Currency from, Currency to, List<ExchangeRateBO> ratesToReturn) {
        if (!from.equals(to)) {
            String rateFrom = resolveRate(from);
            String rateTo = resolveRate(to);
            if (!rateFrom.equals(rateTo)) {
                ratesToReturn.add(new ExchangeRateBO(from, rateFrom, to, rateTo, LocalDate.now(), "International Currency Exchange Market"));
            }
        }
    }

    private String resolveRate(Currency currency) {
        HashMap<Currency, String> map = new HashMap<>();
        map.put(EUR, "1");
        map.put(USD, "1.2");
        map.put(CHF, "0.5");
        map.put(GBP, "0.8");
        return map.get(currency);
    }

    private PaymentBO getPayment(PaymentTypeBO type, Currency debtor, Currency amount, Currency creditor, Currency creditor2) {
        return new PaymentBO("pmt1", false, null,
                null, type, "sepa-credit-transfers", null, null, null, null,
                null, getReference(debtor), ACSP, getTargets(amount, creditor, creditor2), getDepositAccount().getId());
    }

    private List<PaymentTargetBO> getTargets(Currency amount, Currency curr1, Currency curr2) {
        return Stream.of(curr1, curr2)
                       .filter(Objects::nonNull)
                       .map(this::getReference)
                       .map(r -> new PaymentTargetBO(nextTargetId(), "END-TO-END", getAmount(amount), r, null, "name", null, null, null, null, null))
                       .collect(Collectors.toList());
    }

    private AmountBO getAmount(Currency currency) {
        return new AmountBO(currency, BigDecimal.TEN);
    }

    private AccountReferenceBO getReference(Currency currency) {
        AccountReferenceBO reference = new AccountReferenceBO();
        reference.setIban(IBAN);
        reference.setCurrency(currency);
        return reference;
    }

    private String nextTargetId() {
        PMT_ID++;
        return "target" + PMT_ID;
    }

    private static LedgerBO getLedger() {
        LedgerBO ledgerBO = new LedgerBO();
        ledgerBO.setName("ledger");
        return ledgerBO;
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                "name", "product", null, AccountType.CASH, AccountStatus.ENABLED, "bic", null,
                AccountUsage.PRIV, "details");
    }

    private DepositAccountDetailsBO getDepositAccountBO() {
        return new DepositAccountDetailsBO(
                new DepositAccountBO("id", "iban", null, null, null, "msisdn", EUR, "name", "product", AccountTypeBO.CASH, AccountStatusBO.ENABLED, "bic", null, AccountUsageBO.PRIV, "details"),
                Collections.emptyList());
    }
}

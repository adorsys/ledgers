package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.postings.api.exception.PostingErrorCode.POSTING_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountServiceImplTest {
    private final static String ACCOUNT_ID = "ACCOUNT_ID";
    private final static String POSTING_ID = "posting_ID";
    private static final String SYSTEM = "System";
    private static final Currency EUR = Currency.getInstance("EUR");
    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;

    private DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);
    @Mock
    private PostingService postingService;
    @Mock
    private TransactionDetailsMapper transactionDetailsMapper;
    @Mock
    private AccountStmtService accountStmtService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DepositAccountServiceImpl depositAccountService;

    private static final ObjectMapper STATIC_MAPPER = new ObjectMapper()
                                                              .findAndRegisterModules()
                                                              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                                                              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                                                              .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                              .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                                                              .registerModule(new Jdk8Module())
                                                              .registerModule(new JavaTimeModule())
                                                              .registerModule(new ParameterNamesModule());

    @Test
    public void createDepositAccount() {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(getLedgerAccountBO().getName());
        when(ledgerService.newLedgerAccount(any(), anyString())).thenReturn(getLedgerAccountBO());
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount());
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));

        DepositAccountBO depositAccount = depositAccountService.createDepositAccount(getDepositAccountBO(), SYSTEM);

        assertThat(depositAccount).isNotNull();
        assertThat(depositAccount.getId()).isNotBlank();
    }

    @Test
    public void getDepositAccountById() {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(getDepositAccount()));
        //When
        DepositAccountDetailsBO depositAccountDetailsBO = depositAccountService.getDepositAccountById("id", LocalDateTime.now(), false);
        //Then
        assertThat(depositAccountDetailsBO).isNotNull();
        assertThat(depositAccountDetailsBO.getAccount()).isNotNull();
    }

    @Test(expected = DepositModuleException.class)
    public void getDepositAccountById_wrong_id() {
        when(depositAccountRepository.findById("wrong_id")).thenReturn(Optional.empty());
        //When
        depositAccountService.getDepositAccountById("wrong_id", LocalDateTime.now(), false);
    }

    @Test
    public void getDepositAccountByIBAN() {
        when(depositAccountRepository.findByIbanIn(any())).thenReturn(Collections.singletonList(getDepositAccount()));
        //When
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getDepositAccountByIban("iban", LocalDateTime.now(), false);
        //Then
        assertThat(accountDetailsBO).isNotNull();
        assertThat(accountDetailsBO.getAccount()).isNotNull();
    }

    @Test
    public void findDepositAccountsByBranch() {
        when(depositAccountRepository.findByBranch(any())).thenReturn(Collections.singletonList(getDepositAccount()));

        List<DepositAccountDetailsBO> accounts = depositAccountService.findByBranch(anyString());

        // account detail collection is not null
        assertThat(accounts).isNotNull();

        // account detail collection is not empty
        assertThat(accounts).isNotEmpty();
    }

    @Test
    public void getTransactionById() {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(new LedgerBO()));
        when(postingService.findPostingLineById(any(), any())).thenReturn(new PostingLineBO());
        when(transactionDetailsMapper.toTransactionSigned(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        TransactionDetailsBO result = depositAccountService.getTransactionById(ACCOUNT_ID, POSTING_ID);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readFile(TransactionDetailsBO.class, "Transaction.yml"));

    }

    @Test(expected = PostingModuleException.class)
    public void getTransactionById_Failure() {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
        when(depositAccountConfigService.getLedger()).thenReturn("name");
        when(ledgerService.findLedgerByName("name")).thenReturn(Optional.of(new LedgerBO()));
        when(ledgerService.findLedgerAccount(any(), any())).thenReturn(new LedgerAccountBO());
        when(postingService.findPostingLineById(any(), any())).thenThrow(PostingModuleException.builder()
                                                                                 .errorCode(POSTING_NOT_FOUND)
                                                                                 .devMsg(String.format("Could not find posting by ac id: %s and posting id: %s", ACCOUNT_ID, POSTING_ID))
                                                                                 .build());

        depositAccountService.getTransactionById(ACCOUNT_ID, POSTING_ID);
    }

    @Test
    public void getTransactionsByDates() throws JsonProcessingException {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(new DepositAccount()));
        when(postingService.findPostingsByDates(any(), any(), any())).thenReturn(Collections.singletonList(newPostingLineBO()));
        when(transactionDetailsMapper.toTransactionSigned(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        List<TransactionDetailsBO> result = depositAccountService.getTransactionsByDates(ACCOUNT_ID, LocalDateTime.of(2018, 12, 12, 0, 0), LocalDateTime.of(2018, 12, 18, 0, 0));
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    public void confirmationOfFunds_more_than_necessary_available() {
        confirmationOfFunds_more_than_necessary_available(100);
        confirmationOfFunds_more_than_necessary_available(101);
    }

    private void confirmationOfFunds_more_than_necessary_available(long amount) {
        when(depositAccountRepository.findByIbanIn(any())).thenReturn(Collections.singletonList(getDepositAccount()));
        when(accountStmtService.readStmt(any(), any())).thenReturn(newAccountStmtBO(amount));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        boolean response = depositAccountService.confirmationOfFunds(readFile(FundsConfirmationRequestBO.class, "FundsConfirmationRequest.yml"));
        assertThat(response).isTrue();
    }

    @Test(expected = DepositModuleException.class)
    public void confirmationOfFunds_Failure() {
        when(depositAccountRepository.findByIbanIn(any())).thenReturn(Collections.emptyList());
        depositAccountService.confirmationOfFunds(readFile(FundsConfirmationRequestBO.class, "FundsConfirmationRequest.yml"));
    }

    private PostingLineBO newPostingLineBO() throws JsonProcessingException {
        PostingLineBO pl = new PostingLineBO();
        pl.setAccount(new LedgerAccountBO());
        pl.setDetails(STATIC_MAPPER.writeValueAsString(new TransactionDetailsBO()));
        return pl;
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                "name", "product", null, AccountType.CASH, AccountStatus.ENABLED, "bic", null,
                AccountUsage.PRIV, "details");
    }

    private DepositAccountBO getDepositAccountBO() {
        DepositAccountBO bo = new DepositAccountBO();
        bo.setId("id");
        bo.setIban("iban");
        bo.setMsisdn("msisdn");
        bo.setCurrency(Currency.getInstance("EUR"));
        bo.setName("name");
        bo.setProduct("product");
        bo.setAccountType(AccountTypeBO.CASH);
        bo.setAccountStatus(AccountStatusBO.ENABLED);
        bo.setBic("bic");
        bo.setUsageType(AccountUsageBO.PRIV);
        bo.setDetails("details");
        return bo;
    }

    private LedgerAccountBO getLedgerAccountBO() {
        LedgerAccountBO bo = new LedgerAccountBO();
        bo.setId("id");
        bo.setName("name");

        return bo;
    }

    private static LedgerBO getLedger() {
        LedgerBO ledgerBO = new LedgerBO();
        ledgerBO.setName("ledger");
        return ledgerBO;
    }

    private AccountStmtBO newAccountStmtBO(long amount) {
        AccountStmtBO result = new AccountStmtBO();
        LedgerAccountBO ledgerAccountBO2 = readFile(LedgerAccountBO.class, "LedgerAccount.yml");
        result.setAccount(ledgerAccountBO2);
        result.setPstTime(LocalDateTime.now());
        result.setTotalCredit(BigDecimal.valueOf(amount));
        return result;
    }

    private <T> T readFile(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    @Test(expected = DepositModuleException.class)
    public void depositCash_accountNotFound() {
        depositAccountService.depositCash(ACCOUNT_ID, new AmountBO(EUR, BigDecimal.TEN), "recordUser");
    }

    @Test
    public void depositCashCreatesPostingWithTransactionAsDetails() throws IOException {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(getDepositAccount()));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        when(objectMapper.writeValueAsString(any())).thenAnswer(i -> STATIC_MAPPER.writeValueAsString(i.getArguments()[0]));
        AmountBO amount = new AmountBO(EUR, BigDecimal.TEN);

        depositAccountService.depositCash(ACCOUNT_ID, amount, "recordUser");

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
}

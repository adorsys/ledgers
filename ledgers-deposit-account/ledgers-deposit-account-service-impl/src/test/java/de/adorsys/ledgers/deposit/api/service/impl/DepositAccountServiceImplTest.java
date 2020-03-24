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
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.hibernate.query.internal.QueryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pro.javatar.commons.reader.YamlReader;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.deposit.db.domain.AccountStatus.*;
import static de.adorsys.ledgers.util.exception.PostingErrorCode.POSTING_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountServiceImplTest {
    private final static String ACCOUNT_ID = "ACCOUNT_ID";
    private final static String POSTING_ID = "posting_ID";
    private static final String SYSTEM = "System";

    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private PostingService postingService;
    @Mock
    private TransactionDetailsMapper transactionDetailsMapper;
    @Mock
    private AccountStmtService accountStmtService;
    @Mock
    private CurrencyExchangeRatesService exchangeRatesService;

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
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount(ENABLED));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));

        DepositAccountBO depositAccount = depositAccountService.createNewAccount(getDepositAccountBO(), SYSTEM, "");

        assertThat(depositAccount).isNotNull();
        assertThat(depositAccount.getId()).isNotBlank();
    }

    @Test(expected = DepositModuleException.class)
    public void createDepositAccount_account_already_exist() {
        when(depositAccountRepository.findByIbanAndCurrency(anyString(),anyString())).thenReturn(Optional.of(getDepositAccount(ENABLED)));

        DepositAccountBO depositAccount = depositAccountService.createNewAccount(getDepositAccountBO(), SYSTEM, "");

        assertThat(depositAccount).isNotNull();
        assertThat(depositAccount.getId()).isNotBlank();
    }

    @Test
    public void getDepositAccountById() {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        //When
        DepositAccountDetailsBO depositAccountDetailsBO = depositAccountService.getAccountDetailsById("id", LocalDateTime.now(), false);
        //Then
        assertThat(depositAccountDetailsBO).isNotNull();
        assertThat(depositAccountDetailsBO.getAccount()).isNotNull();
    }

    @Test(expected = DepositModuleException.class)
    public void getDepositAccountById_wrong_id() {
        when(depositAccountRepository.findById("wrong_id")).thenReturn(Optional.empty());
        //When
        depositAccountService.getAccountDetailsById("wrong_id", LocalDateTime.now(), false);
    }

    @Test
    public void getDepositAccountByIBANAndCurrency() {
        when(depositAccountRepository.findByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        //When
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsByIbanAndCurrency("iban", Currency.getInstance("EUR"), LocalDateTime.now(), false);
        //Then
        assertThat(accountDetailsBO).isNotNull();
        assertThat(accountDetailsBO.getAccount()).isNotNull();
    }

    @Test
    public void checkAccountStatus_enabled() {
        when(depositAccountRepository.findByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        //When
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsByIbanAndCurrency("iban", Currency.getInstance("EUR"), LocalDateTime.now(), false);
        boolean checkAccountStatus = accountDetailsBO.isEnabled();
        //Then
        assertThat(accountDetailsBO).isNotNull();
        assertThat(accountDetailsBO.getAccount()).isNotNull();
        assertThat(checkAccountStatus).isTrue();
    }

    @Test
    public void checkAccountStatus_blocked() {
        when(depositAccountRepository.findByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccount(BLOCKED)));
        //When
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsByIbanAndCurrency("iban", Currency.getInstance("EUR"), LocalDateTime.now(), false);
        boolean checkAccountStatus = accountDetailsBO.isEnabled();
        //Then
        assertThat(accountDetailsBO).isNotNull();
        assertThat(accountDetailsBO.getAccount()).isNotNull();
        assertThat(checkAccountStatus).isFalse();
    }

    @Test
    public void checkAccountStatus_deleted() {
        when(depositAccountRepository.findByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccount(DELETED)));
        //When
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsByIbanAndCurrency("iban", Currency.getInstance("EUR"), LocalDateTime.now(), false);
        boolean checkAccountStatus = accountDetailsBO.isEnabled();
        //Then
        assertThat(accountDetailsBO).isNotNull();
        assertThat(accountDetailsBO.getAccount()).isNotNull();
        assertThat(checkAccountStatus).isFalse();
    }

    @Test
    public void findDepositAccountsByBranch() {
        when(depositAccountRepository.findByBranch(any())).thenReturn(Collections.singletonList(getDepositAccount(ENABLED)));

        List<DepositAccountDetailsBO> accounts = depositAccountService.findDetailsByBranch(anyString());

        // account detail collection is not null
        assertThat(accounts).isNotNull();

        // account detail collection is not empty
        assertThat(accounts).isNotEmpty();
    }

    @Test
    public void getTransactionById() {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
        when(postingService.findPostingLineById(any(), any())).thenReturn(new PostingLineBO());
        when(transactionDetailsMapper.toTransactionSigned(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        TransactionDetailsBO result = depositAccountService.getTransactionById(ACCOUNT_ID, POSTING_ID);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readFile(TransactionDetailsBO.class, "Transaction.yml"));

    }

    @Test(expected = PostingModuleException.class)
    public void getTransactionById_Failure() {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
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
        List<TransactionDetailsBO> result = depositAccountService.getTransactionsByDates(ACCOUNT_ID, LocalDateTime.of(2018, 12, 12, 0, 0), LocalDateTime.of(2018, 12, 18, 0, 0));
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    public void confirmationOfFunds_more_than_necessary_available() {
        confirmationOfFunds_more_than_necessary_available(100);
        confirmationOfFunds_more_than_necessary_available(101);
    }

    private void confirmationOfFunds_more_than_necessary_available(long amount) {
        when(depositAccountRepository.findByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        when(accountStmtService.readStmt(any(), any())).thenReturn(newAccountStmtBO(amount));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        Whitebox.setInternalState(depositAccountService, "exchangeRatesService", new CurrencyExchangeRatesServiceImpl(null, null));
        boolean response = depositAccountService.confirmationOfFunds(readFile(FundsConfirmationRequestBO.class, "FundsConfirmationRequest.yml"));
        assertThat(response).isTrue();
    }

    @Test(expected = DepositModuleException.class)
    public void confirmationOfFunds_Failure() {
        depositAccountService.confirmationOfFunds(readFile(FundsConfirmationRequestBO.class, "FundsConfirmationRequest.yml"));
    }

    @Test
    public void getAccountsByIbanAndParamCurrency() {
        when(depositAccountRepository.findAllByIbanAndCurrencyContaining(anyString(), anyString())).thenReturn(Collections.singletonList(getDepositAccount(ENABLED)));
        List<DepositAccountBO> result = depositAccountService.getAccountsByIbanAndParamCurrency("iban", "EUR");
        assertThat(result).isEqualTo(Collections.singletonList(getDepositAccountBO()));
    }

    @Test
    public void getAccountByIbanAndCurrency() {
        when(depositAccountRepository.findByIbanAndCurrency(anyString(), anyString())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        DepositAccountBO result = depositAccountService.getAccountByIbanAndCurrency("iban", Currency.getInstance("EUR"));
        assertThat(result).isEqualTo(getDepositAccountBO());
    }

    @Test(expected = DepositModuleException.class)
    public void getAccountByIbanAndCurrency_not_found() {
        when(depositAccountRepository.findByIbanAndCurrency(anyString(), anyString())).thenReturn(Optional.empty());
        DepositAccountBO result = depositAccountService.getAccountByIbanAndCurrency("iban", Currency.getInstance("EUR"));
        assertThat(result).isEqualTo(getDepositAccountBO());
    }

    @Test
    public void getAccountById() {
        when(depositAccountRepository.findById(anyString())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        DepositAccountBO result = depositAccountService.getAccountById("accountId");
        assertThat(result).isEqualTo(getDepositAccountBO());
    }

    @Test(expected = DepositModuleException.class)
    public void getAccountById_not_found() {
        when(depositAccountRepository.findById(anyString())).thenReturn(Optional.empty());
        DepositAccountBO result = depositAccountService.getAccountById("accountId");
        assertThat(result).isEqualTo(getDepositAccountBO());
    }

    @Test
    public void getTransactionsByDatesPaged() throws JsonProcessingException {
        when(depositAccountRepository.findById(anyString())).thenReturn(Optional.of(getDepositAccount(ENABLED)));
        when(postingService.findPostingsByDatesPaged(any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(newPostingLineBO())));
        when(transactionDetailsMapper.toTransactionSigned(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        Page<TransactionDetailsBO> result = depositAccountService.getTransactionsByDatesPaged("accountId", LocalDateTime.now(), LocalDateTime.now(), Pageable.unpaged());
        assertThat(result).isEqualTo(new PageImpl<>(Collections.singletonList(readFile(TransactionDetailsBO.class, "Transaction.yml"))));
    }

    @Test
    public void findDetailsByBranchPaged() {
        when(depositAccountRepository.findByBranchAndIbanContaining(anyString(), anyString(), any())).thenReturn(new PageImpl<>(Collections.singletonList(getDepositAccount(ENABLED))));

        Page<DepositAccountDetailsBO> result = depositAccountService.findDetailsByBranchPaged("branchId", "someParam", Pageable.unpaged());
        assertThat(result).isEqualTo(new PageImpl<>(Collections.singletonList(new DepositAccountDetailsBO(getDepositAccountBO(), Collections.emptyList()))));
    }

    private PostingLineBO newPostingLineBO() throws JsonProcessingException {
        PostingLineBO pl = new PostingLineBO();
        pl.setAccount(new LedgerAccountBO());
        pl.setDetails(STATIC_MAPPER.writeValueAsString(new TransactionDetailsBO()));
        return pl;
    }

    private DepositAccount getDepositAccount(AccountStatus status) {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                                  "name", "product", null, AccountType.CASH, status, "bic", "linked",
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
        bo.setLinkedAccounts("linked");
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
}

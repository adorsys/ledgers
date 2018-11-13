package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.*;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.db.repository.PaymentTargetRepository;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountServiceImplTest {
    private final static String ACCOUNT_ID = "ACCOUNT_ID";
    private final static String POSTING_ID = "posting_ID";
    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);
    @Mock
    private PostingService postingService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private TransactionDetailsMapper transactionDetailsMapper;
    @Mock
    PaymentTargetRepository paymentTargetRepository;

    @InjectMocks
    private DepositAccountServiceImpl depositAccountService;

    @Test
    public void createDepositAccount() throws LedgerAccountNotFoundException, LedgerNotFoundException, DepositAccountNotFoundException {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(getLedgerAccountBO().getName());
        when(ledgerService.newLedgerAccount(any())).thenReturn(getLedgerAccountBO());
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount());
        when(depositAccountMapper.toDepositAccount(any())).thenReturn(getDepositAccount());
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));

        DepositAccountBO depositAccount = depositAccountService.createDepositAccount(getDepositAccountBO());

        assertThat(depositAccount).isNotNull();
        assertThat(depositAccount.getId()).isNotBlank();
    }

    @Test
    public void getDepositAccountById() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(getDepositAccount()));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        //When
        DepositAccountBO account = depositAccountService.getDepositAccountById("id");
        //Then
        assertThat(account).isNotNull();
    }

    @Test(expected = DepositAccountNotFoundException.class)
    public void getDepositAccountById_wrong_id() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findById("wrong_id")).thenReturn(Optional.empty());
        //When
        depositAccountService.getDepositAccountById("wrong_id");
    }

    @Test
    public void getDepositAccountByIBAN() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findByIban(any())).thenReturn(Optional.of(getDepositAccount()));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        //When
        DepositAccountBO account = depositAccountService.getDepositAccountByIBAN("iban");
        //Then
        assertThat(account).isNotNull();
    }

    @Test
    public void getTransactionById() throws TransactionNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException, LedgerNotFoundException {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(readFile(DepositAccountBO.class, "DepositAccount.yml"));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(new LedgerBO()));
        when(paymentTargetRepository.findById(any())).thenReturn(Optional.of(getTarget()));
        when(postingService.findPostingById(any(), any())).thenReturn(new PostingLineBO());
        when(transactionDetailsMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        TransactionDetailsBO result = depositAccountService.getTransactionById(ACCOUNT_ID, POSTING_ID);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readFile(TransactionDetailsBO.class, "Transaction.yml")); //TODO uncomment after implementation of transactionMapper

    }

    private PaymentTarget getTarget() {
        PaymentTarget target = new PaymentTarget();
        Payment payment = new Payment();
        payment.setPaymentId("payment_id");
        target.setPayment(payment);
        return target;
    }

    @Test(expected = TransactionNotFoundException.class)
    public void getTransactionById_Failure() throws TransactionNotFoundException, LedgerNotFoundException, LedgerAccountNotFoundException {
        when(depositAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(readFile(DepositAccount.class, "DepositAccount.yml")));
        when(depositAccountConfigService.getLedger()).thenReturn("name");
        when(ledgerService.findLedgerByName("name")).thenReturn(Optional.of(new LedgerBO()));
        when(ledgerService.findLedgerAccount(any(), any())).thenReturn(new LedgerAccountBO());
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(readFile(DepositAccountBO.class, "DepositAccount.yml"));

        depositAccountService.getTransactionById(ACCOUNT_ID, POSTING_ID);
    }

    @Test
    public void getTransactionsByDates() throws DepositAccountNotFoundException, LedgerAccountNotFoundException, LedgerNotFoundException, JsonProcessingException {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(new DepositAccount()));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(new DepositAccountBO());
        when(postingService.findPostingsByDates(any(), any(), any())).thenReturn(Collections.singletonList(newPostingLineBO()));
        when(transactionDetailsMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));
        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(getLedger()));
        List<TransactionDetailsBO> result = depositAccountService.getTransactionsByDates(ACCOUNT_ID, LocalDateTime.of(2018, 12, 12, 0, 0), LocalDateTime.of(2018, 12, 18, 0, 0));
        assertThat(result.isEmpty()).isFalse();
    }

    private PostingLineBO newPostingLineBO() throws JsonProcessingException {
        PostingLineBO pl = new PostingLineBO();
        pl.setAccount(new LedgerAccountBO());
        pl.setDetails(SerializationUtils.writeValueAsString(new TransactionDetailsBO()));
        return pl;
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", Currency.getInstance("EUR"),
                "name", "product", AccountType.CASH, AccountStatus.ENABLED, "bic", null,
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


    private <T> T readFile(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}
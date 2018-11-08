package de.adorsys.ledgers.deposit.api.service.impl.mockbank;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.impl.test.DepositAccountServiceApplication;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.exception.LedgerWithIdNotFoundException;
import de.adorsys.ledgers.postings.db.exception.PostingRepositoryException;

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DepositAccountServiceApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
public class DepositAccountServiceImplIT {

    @Configuration
    static class Config {
        @Bean
        public ASPSPConfigSource configSource() {
            return new MockBankConfigSource();
        }
    }

    @Autowired
    private AccountStmtService accountStmtService;
    @Autowired
    private PostingService postingService;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private DepositAccountConfigService depositAccountConfigService;

    @Autowired
    private DepositAccountService depositAccountService;

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Testing the test. Negative case, if comparison with wrong balance works.
     *
     * @throws IOException
     * @throws DoubleEntryAccountingException
     * @throws PostingRepositoryException
     * @throws BaseLineException
     * @throws LedgerAccountNotFoundException
     * @throws PostingNotFoundException
     * @throws LedgerNotFoundException
     * @throws LedgerWithIdNotFoundException
     */
    @Test
    public void use_case_newbank_no_overriden_tx_nok() throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException {
        loadPosting("use_case_newbank_no_overriden_tx.yml");

        LocalDateTime dateTime = LocalDateTime.of(2018, Month.JANUARY, 01, 23, 59);
        checkBalance("1104", dateTime, new BigDecimal(4200000.00));
        checkBalance("3001", dateTime, new BigDecimal(-4200000.00));
        checkWrongBalance("1104", dateTime, new BigDecimal(0.00));
    }

    /**
     * Classical case, no overriden transaction. Test balance computation.
     *
     * @throws IOException
     * @throws DoubleEntryAccountingException
     * @throws PostingRepositoryException
     * @throws BaseLineException
     * @throws LedgerAccountNotFoundException
     * @throws PostingNotFoundException
     * @throws LedgerNotFoundException
     * @throws LedgerWithIdNotFoundException
     */
    @Test
    public void use_case_newbank_no_overriden_tx_ok() throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException {
        loadPosting("use_case_newbank_no_overriden_tx.yml");

        LocalDateTime dateTime = LocalDateTime.of(2018, Month.JANUARY, 01, 8, 30);
        checkBalance("1104", dateTime, new BigDecimal(0.00));
        checkBalance("3001", dateTime, new BigDecimal(0.00));
        dateTime = LocalDateTime.of(2018, Month.JANUARY, 01, 23, 59);
        checkBalance("1104", dateTime, new BigDecimal(4200000.00));
        checkBalance("3001", dateTime, new BigDecimal(-4200000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 02, 23, 59);
        checkBalance("11240", dateTime, new BigDecimal(2000000.00));
        checkBalance("1104", dateTime, new BigDecimal(2200000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 03, 23, 59);
        checkBalance("11240", dateTime, new BigDecimal(1835600.00));
        checkBalance("1810", dateTime, new BigDecimal(14400.00));
        checkBalance("1001", dateTime, new BigDecimal(150000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 8, 23, 59);
        checkBalance("11240", dateTime, new BigDecimal(1803600.00));
        checkBalance("5057", dateTime, new BigDecimal(32000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 12, 23, 59);
        checkBalance("1001", dateTime, new BigDecimal(156500.00));
        checkBalance("DE69760700240340283600", dateTime, new BigDecimal(-1500.00));
        checkBalance("1006", dateTime, new BigDecimal(3500.00));
        checkBalance("DE80760700240271232400", dateTime, new BigDecimal(-3500.00));
        checkBalance("DE38760700240320465700", dateTime, new BigDecimal(-5000.00));

    }

    @Test
    public void use_case_check_account_balance() throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException {
        loadPosting("use_case_newbank_no_overriden_tx.yml");
        List<BalanceBO> balances = depositAccountService.getBalances("DE38760700240320465700");
        Assert.assertNotNull(balances);
        Assert.assertEquals(1, balances.size());
        BalanceBO balanceBO = balances.iterator().next();
        Assert.assertEquals(new BigDecimal(5000.00).doubleValue(), balanceBO.getAmount().getAmount().doubleValue(), 0d);
    }

    private void checkBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) throws IllegalStateException, LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber);
        BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
        Assert.assertEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }

    private void checkWrongBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber);
        BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
        Assert.assertNotEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }

    private void loadPosting(String s) throws LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException, IOException {
        LedgerBO ledger = loadLedger();
        Assume.assumeNotNull(ledger);
        InputStream inputStream = DepositAccountServiceImplIT.class.getResourceAsStream(s);
        PostingBO[] postings = mapper.readValue(inputStream, PostingBO[].class);
        for (PostingBO p : postings) {
            p.setLedger(ledger);
            p.getLines().forEach(pl -> {
                pl.getAccount().setLedger(ledger);
            });
            try {
                postingService.newPosting(p);
            } catch (Exception e) {
                System.out.println("error adding posting");
            }
        }
    }

    private LedgerBO loadLedger() {
        String ledgerName = depositAccountConfigService.getLedger();
        return ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new IllegalStateException(String.format("Ledger with name %s not found", ledgerName)));
    }

    public LedgerAccountBO loadLedgerAccount(LedgerBO ledger, String accountNumber) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        return ledgerService.findLedgerAccount(ledger, accountNumber);
    }

}

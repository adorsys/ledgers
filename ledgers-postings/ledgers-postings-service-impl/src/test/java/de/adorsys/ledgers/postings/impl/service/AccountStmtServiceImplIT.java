package de.adorsys.ledgers.postings.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.impl.test.PostingsApplication;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.LEDGER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("AccountStmtServiceImplIT-db-create.xml")
@DatabaseTearDown(value = {"AccountStmtServiceImplIT-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
class AccountStmtServiceImplIT {

    private static final String SYSTEM = "System";
    @Autowired
    private AccountStmtService accountStmtService;
    @Autowired
    private PostingService postingService;
    @Autowired
    private LedgerService ledgerService;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }

    @Test
    void test_load_coa_ok() throws IOException {
        // Given
        loadCoa("sample_coa_banking.yml");
        LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        assumeTrue(ledger != null);

        // When
        LedgerAccountBO ledgerAccount = loadLedgerAccount(ledger, "1128");

        // Then
        assertNotNull(ledgerAccount);
    }

    @Test
    void test_load_posting_ok() throws IOException {
        loadCoa("sample_coa_banking.yml");
        loadPosting("sample_posting.yml");
    }

    /**
     * Testing the test. Negative case, if comparison with wrong balance works.
     */
    @Test
    void use_case_newbank_no_overriden_tx_nok() throws IOException {
        loadCoa("sample_coa_banking.yml");
        loadPosting("use_case_newbank_no_overriden_tx.yml");

        LocalDateTime dateTime = LocalDateTime.of(2018, Month.JANUARY, 01, 23, 59);
        checkBalance("1104", dateTime, new BigDecimal(4200000.00));
        checkBalance("3001", dateTime, new BigDecimal(-4200000.00));
        checkWrongBalance("1104", dateTime, new BigDecimal(0.00));
    }

    /**
     * Classical case, no overridden transaction. Test balance computation.
     */
    @Test
    void use_case_newbank_no_overriden_tx_ok() throws IOException {
        loadCoa("sample_coa_banking.yml");
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
        checkBalance("2332001", dateTime, new BigDecimal(-1500.00));
        checkBalance("1006", dateTime, new BigDecimal(3500.00));
        checkBalance("2332002", dateTime, new BigDecimal(-3500.00));
        checkBalance("2332003", dateTime, new BigDecimal(-5000.00));
    }

    @Test
    void use_case_newbank_overriden_amount_ok() throws IOException {
        loadCoa("sample_coa_banking.yml");
        loadPosting("use_case_newbank_overriden_amount.yml");

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
        checkBalance("11240", dateTime, new BigDecimal(1835200.00));
        checkBalance("1810", dateTime, new BigDecimal(14800.00));
        checkBalance("1001", dateTime, new BigDecimal(150000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 8, 23, 59);
        checkBalance("11240", dateTime, new BigDecimal(1803200.00));
        checkBalance("5057", dateTime, new BigDecimal(32000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 12, 23, 59);
        checkBalance("1001", dateTime, new BigDecimal(156500.00));
        checkBalance("2332001", dateTime, new BigDecimal(-1500.00));
        checkBalance("1006", dateTime, new BigDecimal(3500.00));
        checkBalance("2332002", dateTime, new BigDecimal(-3500.00));
        checkBalance("2332003", dateTime, new BigDecimal(-5000.00));
    }

    @Test
    void use_case_newbank_overriden_account_number_ok() throws IOException {
        loadCoa("sample_coa_banking.yml");
        loadPosting("use_case_newbank_overriden_account_number.yml");

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
        checkBalance("11240", dateTime, new BigDecimal(1850000.00));
        checkBalance("1810", dateTime, new BigDecimal(14400.00));
        checkBalance("1001", dateTime, new BigDecimal(135600.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 8, 23, 59);
        checkBalance("11240", dateTime, new BigDecimal(1818000.00));
        checkBalance("5057", dateTime, new BigDecimal(32000.00));

        dateTime = LocalDateTime.of(2018, Month.JANUARY, 12, 23, 59);
        checkBalance("1001", dateTime, new BigDecimal(142100.00));
        checkBalance("2332001", dateTime, new BigDecimal(-1500.00));
        checkBalance("1006", dateTime, new BigDecimal(3500.00));
        checkBalance("2332002", dateTime, new BigDecimal(-3500.00));
        checkBalance("2332003", dateTime, new BigDecimal(-5000.00));
    }

    private void checkBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) {
        LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber);
        BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
        assertEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }

    private void checkWrongBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) {
        LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber);
        BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
        assertNotEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }

    private static class LegAccYamlModel {
        private String shortDesc;
        private String name;
        private AccountCategoryBO category;
        private BalanceSideBO balanceSide;
        private String parent;

        public String getShortDesc() {
            return shortDesc;
        }

        public String getName() {
            return name;
        }

        public AccountCategoryBO getCategory() {
            return category;
        }

        public BalanceSideBO getBalanceSide() {
            return balanceSide;
        }

        public String getParent() {
            return parent;
        }
    }

    private void loadPosting(String s) throws IOException {
        LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");//.orElseThrow(() -> new IllegalStateException());
        assumeTrue(ledger != null);
        InputStream inputStream = AccountStmtServiceImplIT.class.getResourceAsStream(s);
        PostingBO[] postings = mapper.readValue(inputStream, PostingBO[].class);
        for (PostingBO p : postings) {
            p.setLedger(ledger);
            p.getLines().forEach(pl -> {
                pl.getAccount().setLedger(ledger);
            });
            postingService.newPosting(p);
        }
    }

    public LedgerBO loadLedger(String id) {
        return ledgerService.findLedgerById(id).orElseThrow(() -> PostingModuleException.builder()
                                                                          .errorCode(LEDGER_NOT_FOUND)
                                                                          .devMsg(String.format("Ledger with id: %s not found", id))
                                                                          .build());
    }

    public LedgerAccountBO loadLedgerAccount(LedgerBO ledger, String accountNumber) {
        return ledgerService.findLedgerAccount(ledger, accountNumber);
    }

    private void loadCoa(String s) throws IOException {
        LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        assumeTrue(ledger != null);

        InputStream inputStream = AccountStmtServiceImplIT.class.getResourceAsStream(s);
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
        for (LegAccYamlModel model : ledgerAccounts) {
            LedgerAccountBO parent = null;
            if (model.getParent() != null) {
                parent = loadLedgerAccount(ledger, model.getParent());
            }
            String shortDesc = model.getShortDesc();
            String name = model.getName();
            BalanceSideBO balanceSide = model.getBalanceSide() != null ? model.getBalanceSide() : parent.getBalanceSide();
            AccountCategoryBO category = model.getCategory() != null ? model.getCategory() : parent.getCategory();
            String longDesc = null;
            LocalDateTime created = LocalDateTime.now();
//			String user = "francis"; 
            String id = Ids.id();
            ChartOfAccountBO coa = ledger.getCoa();
            LedgerAccountBO la = new LedgerAccountBO();
            la.setId(id);
            la.setCreated(created);
            la.setShortDesc(shortDesc);
            la.setLongDesc(longDesc);
            la.setName(name);
            la.setLedger(ledger);
            la.setParent(parent);
            la.setCoa(coa);
            la.setBalanceSide(balanceSide);
            la.setCategory(category);
            ledgerService.newLedgerAccount(la, SYSTEM);
        }
    }
}

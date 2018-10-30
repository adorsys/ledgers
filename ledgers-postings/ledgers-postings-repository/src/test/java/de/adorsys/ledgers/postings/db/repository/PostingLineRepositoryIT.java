package de.adorsys.ledgers.postings.db.repository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.db.domain.AccountCategory;
import de.adorsys.ledgers.postings.db.domain.BalanceSide;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.postings.db.utils.PostingRepositoryFunctions;
import de.adorsys.ledgers.util.Ids;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("PostingLinesRepositoryIT-db-create.xml")
@DatabaseTearDown(value={"PostingLinesRepositoryIT-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class PostingLineRepositoryIT {
	
	@Autowired
	private PostingRepositoryFunctions fctn;
	
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }
    
    @Test
    public void test_load_coa_ok () throws IOException {
    	loadCoa("sample_coa_banking.yml");
    	Ledger ledger = fctn.loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        Assume.assumeNotNull(ledger);
    	LedgerAccount ledgerAccount = fctn.loadLedgerAccount(ledger, "1128").orElse(null);
    	Assert.assertNotNull(ledgerAccount);
    }

    @Test
    public void test_load_posting_ok () throws IOException {
    	loadCoa("sample_coa_banking.yml");
    	loadPosting("sample_posting.yml");
    }

    /**
     * Testing the test. Negative case, if comparison with wrong balance works.
     * 
     * @throws IOException
     */
    @Test
    public void use_case_newbank_no_overriden_tx_nok () throws IOException {
    	loadCoa("sample_coa_banking.yml");
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
     */
    @Test
    public void use_case_newbank_no_overriden_tx_ok () throws IOException {
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
    public void use_case_newbank_overriden_amount_ok () throws IOException {
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
    public void use_case_newbank_overriden_account_number_ok () throws IOException {
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
    	Ledger ledger = fctn.loadLedger("Zd0ND5YwSzGwIfZilhumPg");
    	LedgerAccount account = fctn.loadLedgerAccount(ledger, accountNumber).orElseThrow(() -> new IllegalStateException());
    	BigDecimal balance = fctn.computeBalance(account, date).debitBalance();
    	Assert.assertEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }
    private void checkWrongBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) {
    	Ledger ledger = fctn.loadLedger("Zd0ND5YwSzGwIfZilhumPg");
    	LedgerAccount account = fctn.loadLedgerAccount(ledger, accountNumber).orElseThrow(() -> new IllegalStateException());
    	BigDecimal balance = fctn.computeBalance(account, date).debitBalance();
    	Assert.assertNotEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }
    


	private void loadCoa(String s) throws IOException{
    	Ledger ledger = fctn.loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        Assume.assumeNotNull(ledger);

        InputStream inputStream = PostingLineRepositoryIT.class.getResourceAsStream(s);
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
        for (LegAccYamlModel model : ledgerAccounts) {
        	LedgerAccount parent = null;
            if(model.getParent()!=null) {
            	parent = fctn.loadLedgerAccount(ledger, model.getParent()).orElseThrow(() -> new IllegalStateException());
            }
            String shortDesc = model.getShortDesc();
			String name = model.getName();
			BalanceSide balanceSide = model.getBalanceSide()!=null?model.getBalanceSide():parent.getBalanceSide();
			AccountCategory category = model.getCategory()!=null?model.getCategory():parent.getCategory();
			String longDesc = null;
			LocalDateTime created = LocalDateTime.now();
			String user = "francis"; 
			String id = Ids.id();
			ChartOfAccount coa = ledger.getCoa();
			LedgerAccount la = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);
			fctn.createLedgerAccount(la);
        }
    }


    private static class LegAccYamlModel {
        private String shortDesc;
        private String name;
        private AccountCategory category;
        private BalanceSide balanceSide;
        private String parent;
		public String getShortDesc() {
			return shortDesc;
		}
		public String getName() {
			return name;
		}
		public AccountCategory getCategory() {
			return category;
		}
		public BalanceSide getBalanceSide() {
			return balanceSide;
		}
		public String getParent() {
			return parent;
		}
    }
	private void loadPosting(String s) throws JsonParseException, JsonMappingException, IOException {
    	Ledger ledger = fctn.loadLedger("Zd0ND5YwSzGwIfZilhumPg");//.orElseThrow(() -> new IllegalStateException());
        Assume.assumeNotNull(ledger);
        InputStream inputStream = PostingLineRepositoryIT.class.getResourceAsStream(s);
        Posting[] postings = mapper.readValue(inputStream, Posting[].class);
        for (Posting p : postings) {
        	fctn.newPosting(ledger, p);
		}
	}

    
}

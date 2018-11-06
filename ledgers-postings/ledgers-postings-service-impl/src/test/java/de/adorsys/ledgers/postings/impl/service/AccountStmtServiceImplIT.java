package de.adorsys.ledgers.postings.impl.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
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
import de.adorsys.ledgers.postings.impl.test.PostingsApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("AccountStmtServiceImplIT-db-create.xml")
@DatabaseTearDown(value={"AccountStmtServiceImplIT-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class AccountStmtServiceImplIT {
	
	@Autowired
	private AccountStmtService accountStmtService;
	@Autowired
	private PostingService postingService;
	@Autowired
	private LedgerService ledgerService;
	
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }
    
    @Test
    public void test_load_coa_ok () throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException {
    	loadCoa("sample_coa_banking.yml");
    	LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        Assume.assumeNotNull(ledger);
    	LedgerAccountBO ledgerAccount = loadLedgerAccount(ledger, "1128").orElse(null);
    	Assert.assertNotNull(ledgerAccount);
    }

    @Test
    public void test_load_posting_ok () throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException{
    	loadCoa("sample_coa_banking.yml");
    	loadPosting("sample_posting.yml");
    }

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
    public void use_case_newbank_no_overriden_tx_nok () throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException{
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
     * @throws DoubleEntryAccountingException 
     * @throws PostingRepositoryException 
     * @throws BaseLineException 
     * @throws LedgerAccountNotFoundException 
     * @throws PostingNotFoundException 
     * @throws LedgerNotFoundException 
     * @throws LedgerWithIdNotFoundException 
     */
    @Test
    public void use_case_newbank_no_overriden_tx_ok () throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException{
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
    public void use_case_newbank_overriden_amount_ok () throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException{
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
    public void use_case_newbank_overriden_account_number_ok () throws IOException, DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException{
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

    private void checkBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) throws IllegalStateException, LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException{
    	LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
    	LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber).orElseThrow(() -> new IllegalStateException());
    	BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
    	Assert.assertEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
    }
    private void checkWrongBalance(String accountNumber, LocalDateTime date, BigDecimal expectedBalance) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException{
    	LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
    	LedgerAccountBO account = loadLedgerAccount(ledger, accountNumber).orElseThrow(() -> new IllegalStateException());
    	BigDecimal balance = accountStmtService.readStmt(account, date).debitBalance();
    	Assert.assertNotEquals(expectedBalance.doubleValue(), balance.doubleValue(), 0d);
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
	private void loadPosting(String s) throws LedgerNotFoundException, PostingNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException, IOException {
    	LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");//.orElseThrow(() -> new IllegalStateException());
        Assume.assumeNotNull(ledger);
        InputStream inputStream = AccountStmtServiceImplIT.class.getResourceAsStream(s);
        PostingBO[] postings = mapper.readValue(inputStream, PostingBO[].class);
        for (PostingBO p : postings) {
        	p.setLedger(ledger);
        	p.getLines().stream().forEach(pl -> {
        		pl.getAccount().setLedger(ledger);
        	});
        	postingService.newPosting(p);
		}
	}

	public LedgerBO loadLedger(String id) throws LedgerNotFoundException {
		return ledgerService.findLedgerById(id).orElseThrow(() -> new LedgerNotFoundException(id));
	}

	public Optional<LedgerAccountBO> loadLedgerAccount(LedgerBO ledger, String accountNumber) throws LedgerNotFoundException {
		return ledgerService.findLedgerAccount(ledger, accountNumber);
	}
	
	private void loadCoa(String s) throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException{
    	LedgerBO ledger = loadLedger("Zd0ND5YwSzGwIfZilhumPg");
        Assume.assumeNotNull(ledger);

        InputStream inputStream = AccountStmtServiceImplIT.class.getResourceAsStream(s);
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
        for (LegAccYamlModel model : ledgerAccounts) {
        	LedgerAccountBO parent = null;
            if(model.getParent()!=null) {
            	parent = loadLedgerAccount(ledger, model.getParent()).orElseThrow(() -> new IllegalStateException());
            }
            String shortDesc = model.getShortDesc();
			String name = model.getName();
			BalanceSideBO balanceSide = model.getBalanceSide()!=null?model.getBalanceSide():parent.getBalanceSide();
			AccountCategoryBO category = model.getCategory()!=null?model.getCategory():parent.getCategory();
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
			ledgerService.newLedgerAccount(la);
        }
    }

}

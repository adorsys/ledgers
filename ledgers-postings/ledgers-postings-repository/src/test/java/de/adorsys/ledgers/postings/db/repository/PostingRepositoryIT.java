package de.adorsys.ledgers.postings.db.repository;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingType;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.postings.db.utils.RecordHashHelper;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.hash.HashGenerationException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("PostingRepositoryIT-db-entries.xml")
@DatabaseTearDown(value={"PostingRepositoryIT-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class PostingRepositoryIT {

	ObjectMapper om = new ObjectMapper();
	
	@Autowired
	private PostingRepository postingRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;

	@Test
	public void test_create_posting_ok() {
		Optional<Ledger> ledgerOption = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
		Assume.assumeTrue(ledgerOption.isPresent());
		Posting p = new Posting();
		p.setRecordUser("recUser");
		p.setOprId("oprId");
		p.setPstTime(LocalDateTime.now());
		p.setPstType(PostingType.BAL_STMT);
		p.setLedger(ledgerOption.get());
		p.setOprDetails("oprDetails");
		p.setId(Ids.id());
		postingRepository.save(p);
	}

	@Test
	public void test_load_posting_by_id_ok() {
		Optional<Posting> posting = postingRepository.findById("Zd0ND5YwSzGwIfZilhumPg_POSTING");
		Assume.assumeTrue(posting.isPresent());
	}

	@Test
	public void test_find_posting_by_operation_id() {
		List<Posting> posting = postingRepository.findByOprId("Zd0ND5YwSzGwIfZilhumPg_OPERATION");
		assertEquals(2, posting.size());
	}

	@Test
	public void test_find_first_optional_by_ledger_order_by_record_time_desc() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);

		Posting posting = postingRepository.findFirstByLedgerOrderByRecordTimeDesc(ledger).orElse(null);
		Assume.assumeNotNull(posting);
		assertEquals("Zd0ND5YwSzGwIfZilhumPg_POSTING2", posting.getId());
		System.out.println(posting.getId());

	}

	@Test
	public void test_posting_hash() throws JsonProcessingException, HashGenerationException {
		Optional<Ledger> ledgerOptions = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
		Assume.assumeTrue(ledgerOptions.isPresent());
		Posting p = new Posting();
		p.setRecordUser("recUser");
		p.setOprId("oprId");
		p.setPstTime(LocalDateTime.now());
		p.setPstType(PostingType.BAL_STMT);
		p.setLedger(ledgerOptions.get());
		p.setOprDetails("oprDetails");
		p.setId(Ids.id());
		Posting saved = postingRepository.save(p);
		saved = postingRepository.save(saved.hash());
		
		String writeValueAsString = om.writeValueAsString(saved);
		
		Posting found = postingRepository.findById(saved.getId()).orElse(null);
		String recHash = found.getHash();

		String writeValueAsString2 = om.writeValueAsString(found);
		Assert.assertEquals(writeValueAsString, writeValueAsString2);
		
		RecordHashHelper recordHashHelper = new RecordHashHelper();
		found.setHash(null);
		String computedRecHash = recordHashHelper.computeRecHash(found);
		
		Assert.assertEquals(recHash, computedRecHash);
	}
}

package de.adorsys.ledgers.postings.repository;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import javafx.geometry.Pos;
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

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingType;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.utils.RecordHashHelper;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITPostingRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITPostingRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITPostingRepositoryTest {

	ObjectMapper om = new ObjectMapper();
	
	@Autowired
	private PostingRepository postingRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;

	@Test
	public void test_create_posting_ok() {
		Optional<Ledger> ledgerOptions = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
		Assume.assumeTrue(ledgerOptions.isPresent());
		Posting posting = Posting.builder()
				.id(Ids.id())
				.recordUser("recUser")
				.oprId("oprId")
				.oprDetails("oprDetails")
				.pstTime(LocalDateTime.now())
				.pstType(PostingType.BAL_STMT)
				.ledger(ledgerOptions.get())
				.build();
		postingRepository.save(posting);
	}

	@Test
	public void test_load_posting_by_id_ok() {
		Optional<Posting> posting = postingRepository.findById("Zd0ND5YwSzGwIfZilhumPg_POSTING");
		Assume.assumeTrue(posting.isPresent());
	}

	@Test
	public void test_find_posting_by_operation_id() {

	}

	@Test
	public void test_find_first_optional_by_ledger_order_by_record_time_desc() {

	}

	@Test
	public void test_posting_hash() throws NoSuchAlgorithmException, JsonProcessingException {
		Optional<Ledger> ledgerOptions = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
		Assume.assumeTrue(ledgerOptions.isPresent());
		Posting posting = Posting.builder()
			.id(Ids.id())
			.recordUser("recUser")
			.oprId("oprId")
			.oprDetails("oprDetails")
			.pstTime(LocalDateTime.now())
			.pstType(PostingType.BAL_STMT)
			.ledger(ledgerOptions.get())
			.build();
		
		Posting saved = postingRepository.save(posting);
		
		String writeValueAsString = om.writeValueAsString(saved);
		
		Posting found = postingRepository.findById(posting.getId()).orElse(null);
		String recHash = found.getRecordHash();

		String writeValueAsString2 = om.writeValueAsString(found);
		Assert.assertEquals(writeValueAsString, writeValueAsString2);
		
		RecordHashHelper recordHashHelper = new RecordHashHelper();
		found.setRecordHash(null);
		String computedRecHash = recordHashHelper.computeRecHash(found);
		
		Assert.assertEquals(recHash, computedRecHash);
	}
}

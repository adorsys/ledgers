package de.adorsys.ledgers.postings.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.postings.domain.OpNote;
import de.adorsys.ledgers.tests.PostingsApplication;
import de.adorsys.ledgers.util.Ids;

import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class})
@DatabaseSetup("ITOpNoteRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITOpNoteRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
public class ITOpNoteRepositoryTest {

	@Autowired
	private OpNoteRepository opNoteRepository;
	
	@Test
	public void test_create_op_note_ok() {
		OpNote opNote = OpNote.builder().id(Ids.id()).content("Sample content").recId("rec id").build();
		opNoteRepository.save(opNote);
	}

	@Test
	public void test_load_op_note_ok() {
		OpNote opNote = opNoteRepository.findById("ci8k8zskTrCsi-F3sT3i-g-Note").orElse(null);
		Assert.assertNotNull(opNote);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_op_note_constrain_violation_no_rec_id() {
		OpNote opNote = OpNote.builder().id(Ids.id()).content("Sample content").build();
		opNoteRepository.save(opNote);
	}
}

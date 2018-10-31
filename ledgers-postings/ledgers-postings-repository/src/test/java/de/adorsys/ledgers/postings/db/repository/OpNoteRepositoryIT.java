package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.db.domain.OpNote;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@Ignore
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class})
@DatabaseSetup("OpNoteRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"OpNoteRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
public class OpNoteRepositoryIT {

	@Autowired
	private OpNoteRepository opNoteRepository;
	
	@Test
	public void test_create_op_note_ok() {
		String id = Ids.id();
		String recId = "rec id";
		String type = null;
		String content = "Sample content";
		LocalDateTime recTime = null;
		LocalDateTime execTime = null;
		Boolean prematureExc = null;
		Boolean repeatedExec = null;
		String execStatus = null;
		//OpNote.builder().id(Ids.id()).content("Sample content").recId("rec id").build();
		OpNote opNote = new OpNote(id, recId, type, content, recTime, execTime, prematureExc, repeatedExec, execStatus);
		opNoteRepository.save(opNote);
	}

	@Test
	public void test_load_op_note_ok() {
		OpNote opNote = opNoteRepository.findById("ci8k8zskTrCsi-F3sT3i-g-Note").orElse(null);
		Assert.assertNotNull(opNote);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_op_note_constrain_violation_no_rec_id() {
		String id = Ids.id();
		String recId = null;
		String type = null;
		String content = "Sample content";
		LocalDateTime recTime = null;
		LocalDateTime execTime = null;
		Boolean prematureExc = null;
		Boolean repeatedExec = null;
		String execStatus = null;
//		OpNote opNote = OpNote.builder().id(Ids.id()).content("Sample content").build();
		OpNote opNote = new OpNote(id, recId, type, content, recTime, execTime, prematureExc, repeatedExec, execStatus);
		opNoteRepository.save(opNote);
	}
}

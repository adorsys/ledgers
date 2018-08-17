package de.adorsys.ledgers.postings.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.postings.domain.OpNote;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

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

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_op_note_constrain_violation_no_rec_id() {
		OpNote opNote = OpNote.builder().id(Ids.id()).content("Sample content").build();
		opNoteRepository.save(opNote);
	}
}

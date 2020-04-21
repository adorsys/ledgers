package de.adorsys.ledgers.postings.db.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.db.domain.OpNote;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("OpNoteRepositoryIT-db-entries.xml")
@DatabaseTearDown(value = {"OpNoteRepositoryIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
class OpNoteRepositoryIT {

    @Autowired
    private OpNoteRepository opNoteRepository;

    @Test
    void test_create_op_note_ok() {
        // Given
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

        // When
        opNoteRepository.save(opNote);
    }

    @Test
    void test_load_op_note_ok() {
        // When
        OpNote opNote = opNoteRepository.findById("ci8k8zskTrCsi-F3sT3i-g-Note").orElse(null);

        // Then
        assertNotNull(opNote);
    }

    @Test
    void test_create_op_note_constrain_violation_no_rec_id() {
        // Given
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

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> opNoteRepository.save(opNote));
    }
}

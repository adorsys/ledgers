package de.adorsys.ledgers.postings.repository;

import java.util.Optional;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.PostingTrace;
import de.adorsys.ledgers.tests.PostingsApplication;
import de.adorsys.ledgers.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITPostingTraceRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITPostingTraceRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITPostingTraceTest {

    @Autowired
    private PostingTraceRepository postingTraceRepository;

    @Test
    public void test_create_posting_trace_ok() {
        PostingTrace postingTrace = PostingTrace.builder()
                .id(Ids.id())
                .pos(2)
                .srcPstId("Zd0ND5YwSzGwIfZilhumPg_POSTING")
                .srcPstHash("6b1672f7edf8c371860136271f89b41f22dc122a")
                .tgtPstId("Zd0ND5YwSzGwIfZilhumPg_POSTING2")
                .antTraceId("Zd0ND5YwSzGwIfZilhumPg_POSTING_TRACE_1")
                .antTraceHash("0000000000000000000000000000000000000000")
                .build();

        postingTraceRepository.save(postingTrace);


    }

    @Test
    public void test_load_posting_trace_ok() {
        Optional<PostingTrace> postingTrace = postingTraceRepository.findById("Zd0ND5YwSzGwIfZilhumPg_POSTING_TRACE");
        Assume.assumeTrue(postingTrace.isPresent());
    }

//    @Test(expected=DataIntegrityViolationException.class)
    public void test_create_op_note_constrain_violation_no_rec_id() {
//        OpNote opNote = OpNote.builder().id(Ids.id()).content("Sample content").build();
//        postingTraceRepository.save(opNote);
    }
}

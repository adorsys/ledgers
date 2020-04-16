package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
class LedgerEntityRepositoryIT {
    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

    @Test
    void test_create_coa_create_timestamp_auto_generated() {
        String id = Ids.id();

        LocalDateTime created = null;
        String user = "Francis";
        String shortDesc = null;
        String longDesc = null;
        ChartOfAccount coa = new ChartOfAccount(id, created, user, shortDesc, longDesc, id);

        chartOfAccountRepository.save(coa);
        coa = chartOfAccountRepository.findById(id).orElse(null);
        assumeTrue(coa != null);
        assertNotNull(coa.getCreated());
    }
}

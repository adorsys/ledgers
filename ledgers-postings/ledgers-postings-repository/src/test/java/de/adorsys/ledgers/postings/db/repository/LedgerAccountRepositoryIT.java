package de.adorsys.ledgers.postings.db.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.db.domain.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("LedgerAccountRepositoryIT-db-entries.xml")
@DatabaseTearDown(value = {"LedgerAccountRepositoryIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
class LedgerAccountRepositoryIT {

    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Test
    void test_create_ledger_account_ok() {
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);

        LedgerAccount parentAccount = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "3.0.0").orElse(null);
        assumeTrue(parentAccount != null);

        LedgerAccount ledgerAccount = new LedgerAccount(Ids.id(), LocalDateTime.now(), "Sample User", "Long lasting liability",
                                                        "Long lasting liability (from 1 year to 3 years)", "Long lasting liability", ledger, parentAccount, ledger.getCoa(), AccountCategory.LI.getDefaultBs(), AccountCategory.LI);
        LedgerAccount account = ledgerAccountRepository.save(ledgerAccount);
        assertNotNull(account);
    }

    @Test
    void test_create_ledger_account_no_ledger() {
        // Given
        String id = Ids.id();
        String name = "Sample Ledger Account";
        String user = "Sample User";
        LocalDateTime created = null;
        LedgerAccount parent = null;
        Ledger ledger = null;
        String shortDesc = null;
        String longDesc = null;
        ChartOfAccount coa = null;
        AccountCategory category = null;
        BalanceSide balanceSide = null;
        LedgerAccount ledgerAccount = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> ledgerAccountRepository.save(ledgerAccount));
    }

    @Test
    void test_create_ledger_account_unique_constrain_violation_ledger_name_validFrom() {
        // Given
        LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_3_0_0").orElse(null);
        assumeTrue(ledgerAccount != null);

        String id = Ids.id();
        LocalDateTime created = null;
        String name = ledgerAccount.getName();
        String user = "Sample User";
        LedgerAccount parent = ledgerAccount.getParent();
        Ledger ledger = ledgerAccount.getLedger();
        String shortDesc = "Long lasting liability";
        String longDesc = "Long lasting liability (from 1 year to 3 years)";
        ChartOfAccount coa = ledgerAccount.getLedger().getCoa();
        AccountCategory category = AccountCategory.LI;
        BalanceSide balanceSide = AccountCategory.LI.getDefaultBs();
        LedgerAccount ledgerAccount2 = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> ledgerAccountRepository.save(ledgerAccount2));
    }

    @Test
    void test_find_by_ledger_and_name_ok() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);

        // When
        Optional<LedgerAccount> found = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "1.0.0");

        // Then
        assertTrue(found.isPresent());
    }
}

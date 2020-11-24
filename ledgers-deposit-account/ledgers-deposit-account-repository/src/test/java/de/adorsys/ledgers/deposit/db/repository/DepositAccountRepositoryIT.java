package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.test.DepositAccountRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DepositAccountRepositoryApplication.class)
class DepositAccountRepositoryIT {

    @Autowired
    private DepositAccountRepository repo;

    @Test
    void create_da_mandatory_properties() {
        DepositAccount da = new DepositAccount();
        da.setId(Ids.id());
        da.setCurrency("EUR");
        da.setIban("DE89370400440532013000");
        DepositAccount account = repo.save(da);
        assertNotNull(account);
    }

}

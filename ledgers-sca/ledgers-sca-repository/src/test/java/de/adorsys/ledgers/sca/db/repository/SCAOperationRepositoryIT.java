package de.adorsys.ledgers.sca.db.repository;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.test.SCARepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SCARepositoryApplication.class)
class SCAOperationRepositoryIT {

    @Autowired
    private SCAOperationRepository scaRepo;

    @Test
    void test_create_ok() {
        SCAOperationEntity scaOp = new SCAOperationEntity();
        scaOp.setId(Ids.id());
        scaOp.setAuthCodeHash("asdfdsfa");
        scaOp.setCreated(LocalDateTime.now());
        scaOp.setHashAlg("HS256");
        scaOp.setOpId(Ids.id());
        scaOp.setStatus(AuthCodeStatus.SENT);
        scaOp.setStatusTime(LocalDateTime.now());
        scaOp.setValiditySeconds(300);
        SCAOperationEntity saved = scaRepo.save(scaOp);
        assertNotNull(saved);
    }

}

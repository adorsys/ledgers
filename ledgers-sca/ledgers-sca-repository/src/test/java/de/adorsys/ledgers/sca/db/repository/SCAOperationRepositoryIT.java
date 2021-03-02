package de.adorsys.ledgers.sca.db.repository;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.test.SCARepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SCARepositoryApplication.class)
class SCAOperationRepositoryIT {

    @Autowired
    private SCAOperationRepository scaRepo;

    @Test
    void test_create_ok() {
        SCAOperationEntity scaOp = new SCAOperationEntity(Ids.id(), Ids.id(), null, OpType.CONSENT, null,
                                                          300, 0, null, 100);
        scaOp.setAuthCodeHash("asdfdsfa");
        scaOp.setHashAlg("HS256");
        scaOp.setStatus(AuthCodeStatus.SENT);
        SCAOperationEntity saved = scaRepo.save(scaOp);
        assertNotNull(saved);
    }

}

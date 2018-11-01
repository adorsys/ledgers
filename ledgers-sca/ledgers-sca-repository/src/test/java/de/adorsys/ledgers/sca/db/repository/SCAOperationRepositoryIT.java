package de.adorsys.ledgers.sca.db.repository;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.test.SCARepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SCARepositoryApplication.class)
public class SCAOperationRepositoryIT {
	
	@Autowired
	private SCAOperationRepository scaRepo;

	@Test
	public void test_create_ok() {
		SCAOperationEntity scaOp = new SCAOperationEntity();
		scaOp.setAuthCodeHash("asdfdsfa");
		scaOp.setCreated(LocalDateTime.now());
		scaOp.setHashAlg("HS256");
		scaOp.setOpId(Ids.id());
		scaOp.setStatus(AuthCodeStatus.NEW);
		scaOp.setStatusTime(LocalDateTime.now());
		scaOp.setValiditySeconds(300);
		scaRepo.save(scaOp);
	}

}

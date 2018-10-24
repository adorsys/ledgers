package de.adorsys.ledgers.um.db.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.um.db.domain.ScaUserData;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
@DatabaseSetup("SCAMethodsRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"SCAMethodsRepositoryTest-db-entries.xml"}, type= DatabaseOperation.DELETE_ALL)
public class ScaUserDataIT {

    @Autowired
    private ScaUserDataRepository scaUserDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Test(expected = DataIntegrityViolationException.class)
    public void test_sca_method_and_sca_value_constraint() {
        UserEntity vladi = new UserEntity();
        vladi.setId("SomeUniqueUserID");
        vladi.setEmail("vne@adorsys.de");
        vladi.setLogin("vne");
        vladi.setPin("12345");
        userRepository.save(vladi);

        ScaUserData scaEmail = new ScaUserData();
        scaEmail.setId("SomeUniqueScaID");
        scaEmail.setMethodType("email");
        scaEmail.setMethodValue("SuperSecureCode");
        scaEmail.setUser(vladi);

        scaUserDataRepository.save(scaEmail);

        ScaUserData scaEmailRepeat = new ScaUserData();
        scaEmailRepeat.setId("SomeUniqueScaRepeatID");
        scaEmailRepeat.setMethodType("email");
        scaEmailRepeat.setMethodValue("SuperSecureCode");
        scaEmailRepeat.setUser(vladi);

        scaUserDataRepository.save(scaEmailRepeat);
    }
}

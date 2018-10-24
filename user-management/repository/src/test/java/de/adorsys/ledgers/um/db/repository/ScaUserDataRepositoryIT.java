package de.adorsys.ledgers.um.db.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.um.db.domain.ScaMethodType;
import de.adorsys.ledgers.um.db.domain.ScaUserData;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.util.Ids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
@DatabaseSetup("SCAMethodsRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"SCAMethodsRepositoryTest-db-entries.xml"}, type= DatabaseOperation.DELETE_ALL)
public class ScaUserDataRepositoryIT {

    @Autowired
    private ScaUserDataRepository scaUserDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test_create_ok() {

        ScaUserData data = new ScaUserData();
        data.setId(Ids.id());
        data.setScaMethod(ScaMethodType.EMAIL);
        data.setMethodValue("vne@adorsys.de");

        UserEntity user = new UserEntity();
        user.setId(Ids.id());
        user.setPin("1234");
        user.setLogin("vne");
        user.setEmail("vne@adorsys.de");

        data.setUser(user);
        user.getScaMethods().add(data);
        data.setUser(user);
        userRepository.save(user);
        ScaUserData result = scaUserDataRepository.findById(data.getId()).orElse(null);
        Assert.notNull(result);
    }

}

package de.adorsys.ledgers.um.db.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
@DatabaseSetup("SCAMethodsRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"SCAMethodsRepositoryTest-db-entries.xml"}, type= DatabaseOperation.DELETE_ALL)
public class SCAMethodIT {

//    @Autowired
//    private SCAMethodRepository scaMethodRepository;

    @Test
    public void test_sca_method_and_sca_value_constraint() {

    }
}

package de.adorsys.ledgers.um.service.impl;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.um.UserManagementApplication;
import de.adorsys.ledgers.um.domain.User;
import de.adorsys.ledgers.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DatabaseSetup("UserTestTest-db-entries.xml")
@DatabaseTearDown(value={"UserTestTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
@SpringBootTest(classes= UserManagementApplication.class)
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    public void create_ok() {
        User vladimir = User.builder()
                .email("vne@adorsys.com.ua")
                .pin("12345678")
                .build();

        try {
            userService.create(vladimir);
        } catch (UserAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void create_user_with_the_same_email() throws UserAlreadyExistsException {
        User vladimir = User.builder()
                .email("vne@adorsys.de")
                .pin("12345678")
                .build();

        userService.create(vladimir);
    }

    @Test
    public void findByEmail() {
    }

    @Test
    public void addAccount() {
    }
}

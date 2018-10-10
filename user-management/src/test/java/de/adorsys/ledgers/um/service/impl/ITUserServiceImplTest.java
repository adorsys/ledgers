/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.service.impl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.um.UserManagementApplication;
import de.adorsys.ledgers.um.domain.User;
import de.adorsys.ledgers.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.exception.UserNotFoundException;
import de.adorsys.ledgers.um.repository.UserRepository;
import de.adorsys.ledgers.um.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UserManagementApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("ITUserServiceImplTest-db-entries.xml")
@DatabaseTearDown(value = {"ITUserServiceImplTest-db-entries.xml"}/*, type = DatabaseOperation.DELETE*/)
public class ITUserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void create_ok() {
        User vladimir = User.builder()
                                .id("1")
                                .email("vne@adorsys.com.ua")
                                .pin("12345678")
                                .build();

        try {
            userService.create(vladimir);
        } catch (UserAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void create_user_with_the_same_email() throws UserAlreadyExistsException {
        User vladimir = User.builder()
                                .id("2")
                                .email("vne@adorsys.de")
                                .pin("12345678")
                                .build();

        userService.create(vladimir);
    }

    @Test
    public void addAccount() throws UserNotFoundException {

        userService.addAccount("vne", LedgerAccount.builder().id("ledger_account_1").build());

        User user = userRepository.findById("1").get();

        assertThat(user.getAccounts(), hasSize(1));
    }
}

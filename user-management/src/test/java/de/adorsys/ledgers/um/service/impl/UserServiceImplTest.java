package de.adorsys.ledgers.um.service.impl;

import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.um.domain.User;
import de.adorsys.ledgers.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.exception.UserNotFoundException;
import de.adorsys.ledgers.um.repository.UserRepository;
import de.adorsys.ledgers.util.MD5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;
    private static final String LOGIN = "speex";
    private static final String EMAIL = "spe@adorsys.com.ua";
    private static final String PIN = "1234";
    private static final String LEDGER_ACCOUNT_ID = "1234567890";
    private static final LedgerAccount LEDGER_ACCOUNT = LedgerAccount.builder().id(LEDGER_ACCOUNT_ID).build();
    private static final User USER = User.builder()
                                            .id("1")
                                            .login(LOGIN)
                                            .pin(PIN)
                                            .email(EMAIL)
                                            .accounts(Collections.singletonList(LEDGER_ACCOUNT))
                                            .build();


    @Test
    public void create() throws UserAlreadyExistsException {

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByLoginAndEmail(LOGIN, EMAIL)).thenReturn(Boolean.FALSE);
        when(userRepository.save(userCaptor.capture())).thenReturn(USER);


        userService.create(USER);

        assertThat(userCaptor.getValue().getPin(), not(PIN));
        assertThat(userCaptor.getValue().getPin(), is(MD5Util.encode(PIN)));

        verify(userRepository, times(1)).existsByLoginAndEmail(LOGIN, EMAIL);
        verify(userRepository, times(1)).save(userCaptor.capture());
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void createWithException() throws UserAlreadyExistsException {

        when(userRepository.existsByLoginAndEmail(LOGIN, EMAIL)).thenReturn(true);

        userService.create(USER);

        verify(userRepository, times(1)).existsByLoginAndEmail(LOGIN, EMAIL);
    }

    @Test
    public void authorize() throws UserNotFoundException {
        User userMock = mock(User.class);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userMock));
        when(userMock.getPin()).thenReturn(MD5Util.encode(PIN));

        boolean authorized = userService.authorize(LOGIN, PIN);

        assertThat(authorized, is(Boolean.TRUE));

        verify(userRepository, times(1)).findByLogin(LOGIN);
    }

    @Test
    public void authorizeFalse() throws UserNotFoundException {
        User userMock = mock(User.class);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userMock));
        when(userMock.getPin()).thenReturn("wrong_password");

        boolean authorized = userService.authorize(LOGIN, PIN);

        assertThat(authorized, is(Boolean.FALSE));

        verify(userRepository, times(1)).findByLogin(LOGIN);
    }

    @Test(expected = UserNotFoundException.class)
    public void authorizeWithException() throws UserNotFoundException {
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

        userService.authorize(LOGIN, PIN);
    }

    @Test
    public void authorizationWithAccount() throws UserNotFoundException {
        User userMock = mock(User.class);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userMock));
        when(userMock.getPin()).thenReturn(MD5Util.encode(PIN));
        when(userMock.getAccounts()).thenReturn(Collections.singletonList(LEDGER_ACCOUNT));

        boolean authorized = userService.authorize(LOGIN, PIN, LEDGER_ACCOUNT_ID);

        assertThat(authorized, is(Boolean.TRUE));

        verify(userRepository, times(1)).findByLogin(LOGIN);
    }

    @Test
    public void authorizationWithWrongAccount() throws UserNotFoundException {
        User userMock = mock(User.class);

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userMock));
        when(userMock.getPin()).thenReturn(MD5Util.encode(PIN));
        when(userMock.getAccounts()).thenReturn(Collections.singletonList(LedgerAccount.builder().id("000111").build()));

        boolean authorized = userService.authorize(LOGIN, PIN, LEDGER_ACCOUNT_ID);

        assertThat(authorized, is(Boolean.FALSE));

        verify(userRepository, times(1)).findByLogin(LOGIN);
    }

    @Test
    public void addAccount() throws UserNotFoundException {
        User userMock = mock(User.class);
        List<LedgerAccount> accounts = new ArrayList<>();

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userMock));
        when(userMock.getAccounts()).thenReturn(accounts);

        userService.addAccount(LOGIN, LEDGER_ACCOUNT);

        assertThat(accounts, hasSize(1));
        assertThat(accounts.get(0).getId(), is(LEDGER_ACCOUNT_ID));
    }
}
/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;


import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.domain.Constants.*;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String USER_LOGIN = "login";
    private static final String TPP_LOGIN = "tpp login";
    private static final String IBAN = "DE123456789";
    private static final String ACCOUNT_ID = "account id";
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDateTime CREATED = LocalDateTime.now();

    @InjectMocks
    private AccessService service;

    @Mock
    private UserService userService;
    @Mock
    private KeycloakTokenService tokenService;

    @Test
    void updateAccountAccessNewAccount_new_access() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN, null);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100, AccessTypeTO.OWNER);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertEquals(1, values.iterator().next().size());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100));
    }

    @Test
    void updateAccountAccessNewAccount_existing_access() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(ACCOUNT_ID, IBAN, EUR, 50))), USER_LOGIN, null);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100, AccessTypeTO.DISPOSE);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);

        // Then
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertEquals(1, values.iterator().next().size());
        AccountAccessBO accessBO = getAccessBO(ACCOUNT_ID, IBAN, EUR, 100);
        accessBO.setAccessType(AccessTypeBO.DISPOSE);
        assertThat(captor.getValue()).containsExactlyInAnyOrder(accessBO);
    }

    @Test
    void updateAccountAccessNewAccount_new_access_tpp_add() {
        // Given
        ReflectionTestUtils.setField(service, "finalWeight", 100);
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN, TPP_LOGIN);
        UserBO tppUser = getUserBO(new ArrayList<>(singletonList(getAccessBO("1", IBAN, USD, 100))), TPP_LOGIN, null);
        when(userService.findById(eq(TPP_LOGIN))).thenReturn(tppUser);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100, AccessTypeTO.OWNER);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(2)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertEquals(2, values.size());

        assertEquals(1, values.get(0).size());
        assertThat(values.get(0)).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100));

        assertEquals(2, values.get(1).size());
        assertThat(values.get(1)).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100), getAccessBO("1", IBAN, USD, 100));
    }

    @Test
    void exchangeTokenStartSca_required() {
        when(tokenService.exchangeToken(any(), any(), any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.exchangeTokenStartSca(true, "token");
        assertNotNull(result);
        verify(tokenService, times(1)).exchangeToken(any(), any(), eq(SCOPE_SCA));
    }

    @Test
    void exchangeTokenStartSca_not_required() {
        when(tokenService.exchangeToken(any(), any(), any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.exchangeTokenStartSca(false, "token");
        assertNotNull(result);
        verify(tokenService, times(1)).exchangeToken(any(), any(), eq(SCOPE_FULL_ACCESS));
    }

    @Test
    void exchangeTokenEndSca_ms_enabled_not_complete() {
        ReflectionTestUtils.setField(service, "multilevelScaEnable", true);
        when(tokenService.exchangeToken(any(), any(), any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.exchangeTokenEndSca(false, "token");
        assertNotNull(result);
        verify(tokenService, times(1)).exchangeToken(any(), any(), eq(SCOPE_PARTIAL_ACCESS));
    }

    @Test
    void exchangeTokenEndSca_ms_enabled_complete() throws NoSuchFieldException {
        exchangeTokenTest(true, false, SCOPE_PARTIAL_ACCESS);
        exchangeTokenTest(true, true, SCOPE_FULL_ACCESS);
        exchangeTokenTest(false, false, SCOPE_FULL_ACCESS);
        exchangeTokenTest(false, true, SCOPE_FULL_ACCESS);
        verify(tokenService, times(1)).exchangeToken(any(), any(), eq(SCOPE_PARTIAL_ACCESS));
        verify(tokenService, times(3)).exchangeToken(any(), any(), eq(SCOPE_FULL_ACCESS));
    }

    private void exchangeTokenTest(boolean multiLevel, boolean authCompleted, String expectedScope) {
        ReflectionTestUtils.setField(service, "multilevelScaEnable", multiLevel);
        when(tokenService.exchangeToken(any(), any(), any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.exchangeTokenEndSca(authCompleted, "token");
        assertNotNull(result);
    }

    private UserBO getUserBO(List<AccountAccessBO> accesses, String login, String branch) {
        UserBO userBO = new UserBO(login, "email", "12345");
        userBO.setAccountAccesses(accesses);
        userBO.setBranch(branch);
        return userBO;
    }

    private AccountAccessBO getAccessBO(String accountId, String iban, Currency currency, int scaWeignt) {
        AccountAccessBO acc = new AccountAccessBO(iban, AccessTypeBO.OWNER);
        acc.setAccountId(accountId);
        acc.setCurrency(currency);
        acc.setScaWeight(scaWeignt);
        return acc;
    }

    private DepositAccountBO getDepostAccountBO() {
        return new DepositAccountBO(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "name", "displayName", "product", AccountTypeBO.CACC, null, null, AccountUsageBO.PRIV, "details", false, false, "branch", CREATED, BigDecimal.ZERO);
    }
}

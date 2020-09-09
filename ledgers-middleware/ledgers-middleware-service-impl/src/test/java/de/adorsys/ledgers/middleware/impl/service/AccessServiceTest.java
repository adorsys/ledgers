package de.adorsys.ledgers.middleware.impl.service;


import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String USER_LOGIN = "login";
    private static final String TPP_LOGIN = "tpp login";
    private static final String IBAN = "DE123456789";
    private static final String IBAN_NOT_OWNED = "DE987654321";
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
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100));
    }

    @Test
    void updateAccountAccessNewAccount_existing_access() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(ACCOUNT_ID, IBAN, EUR, 50))), USER_LOGIN, null);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);

        // Then
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100));
    }

    @Test
    void updateAccountAccessNewAccount_new_access_tpp_add() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(service, service.getClass().getDeclaredField("finalWeight"), 100);
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN, TPP_LOGIN);
        UserBO tppUser = getUserBO(new ArrayList<>(singletonList(getAccessBO("1", IBAN, USD, 100))), TPP_LOGIN, null);
        when(userService.findById(eq(TPP_LOGIN))).thenReturn(tppUser);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, 100);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(2)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertThat(values.size() == 2).isTrue();

        assertThat(values.get(0).size() == 1).isTrue();
        assertThat(values.get(0)).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100));

        assertThat(values.get(1).size() == 2).isTrue();
        assertThat(values.get(1)).containsExactlyInAnyOrder(getAccessBO(ACCOUNT_ID, IBAN, EUR, 100), getAccessBO("1", IBAN, USD, 100));
    }

    private AisAccountAccessInfoBO getAisAccess(List<String> ibans) {
        AisAccountAccessInfoBO ais = new AisAccountAccessInfoBO();
        ais.setAccounts(ibans);
        ais.setBalances(Collections.emptyList());
        ais.setTransactions(Collections.emptyList());
        return ais;
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
        return new DepositAccountBO(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "name", "product", AccountTypeBO.CACC, null, null, AccountUsageBO.PRIV, "details", false, false, "branch", CREATED);
    }
}
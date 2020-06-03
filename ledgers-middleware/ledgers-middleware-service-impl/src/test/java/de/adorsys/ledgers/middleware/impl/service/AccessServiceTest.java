package de.adorsys.ledgers.middleware.impl.service;


import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    @Test
    void updateAccountAccessNewAccount_new_access() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN, null);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));
    }

    @Test
    void updateAccountAccessNewAccount_existing_access() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(IBAN, EUR, 50))), USER_LOGIN, null);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);

        // Then
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));
    }

    @Test
    void updateAccountAccessNewAccount_new_access_tpp_add() {
        // Given
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN, TPP_LOGIN);
        UserBO tppUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(IBAN, USD, 100))), TPP_LOGIN, null);
        when(userService.findById(eq(TPP_LOGIN))).thenReturn(tppUser);

        // When
        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(2)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();

        // Then
        assertThat(values.size() == 2).isTrue();

        assertThat(values.get(0).size() == 1).isTrue();
        assertThat(values.get(0)).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));

        assertThat(values.get(1).size() == 2).isTrue();
        assertThat(values.get(1)).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 100));
    }

    @Test
    void filterOwnedAccounts() {
        // When
        List<String> result = service.filterOwnedAccounts(asList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER), getAccessTO(IBAN_NOT_OWNED, USD, AccessTypeTO.READ)));

        // Then
        assertThat(result).containsExactlyInAnyOrder(IBAN);
    }

    @Test
    void filterOwnedAccounts_null() {
        // When
        List<String> result = service.filterOwnedAccounts(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void resolveScaWeightByDebtorAccount() {
        // Given
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));

        // When
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN);

        // Then
        assertThat(result).isEqualTo(100);
    }

    @Test
    void resolveScaWeightByDebtorAccount_not_100() {
        // Given
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 50));

        // When
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN);

        // Then
        assertThat(result).isEqualTo(50);
    }

    @Test
    void resolveScaWeightByDebtorAccount_not_owned() {
        // Given
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 50));

        // When
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN_NOT_OWNED);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void resolveMinimalScaWeightForConsent() {
        // Given
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));

        // When
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(singletonList(IBAN)), accesses);

        // Then
        assertThat(result).isEqualTo(100);
    }

    @Test
    void resolveMinimalScaWeightForConsent_50() {
        // Given
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));

        // When
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(asList(IBAN, IBAN_NOT_OWNED)), accesses);

        // Then
        assertThat(result).isEqualTo(50);
    }

    @Test
    void resolveMinimalScaWeightForConsent_empty() {
        // Given
        List<AccountAccessBO> accesses = new ArrayList<>();

        // When
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(asList(IBAN, IBAN_NOT_OWNED)), accesses);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void userHasAccessToAccount() {
        // When
        boolean result = service.userHasAccessToAccount(getUserTO(singletonList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER))), IBAN);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userHasAccessToAccount_no_access() {
        // When
        boolean result = service.userHasAccessToAccount(getUserTO(singletonList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER))), IBAN_NOT_OWNED);

        // Then
        assertThat(result).isFalse();
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

    private UserTO getUserTO(List<AccountAccessTO> accesses) {
        UserTO user = new UserTO(USER_LOGIN, "email", "12345");
        user.setAccountAccesses(accesses);
        return user;
    }

    private AccountAccessBO getAccessBO(String iban, Currency currency, int scaWeignt) {
        AccountAccessBO acc = new AccountAccessBO(iban, AccessTypeBO.OWNER);
        acc.setAccountId(ACCOUNT_ID);
        acc.setCurrency(currency);
        acc.setScaWeight(scaWeignt);
        return acc;
    }

    private AccountAccessTO getAccessTO(String iban, Currency currency, AccessTypeTO accType) {
        AccountAccessTO acc = new AccountAccessTO();
        acc.setAccountId(ACCOUNT_ID);
        acc.setIban(iban);
        acc.setCurrency(currency);
        acc.setScaWeight(100);
        acc.setAccessType(accType);
        return acc;
    }

    private DepositAccountBO getDepostAccountBO() {
        return new DepositAccountBO(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "name", "product", AccountTypeBO.CACC, null, null, AccountUsageBO.PRIV, "details", false, false, "branch", CREATED);
    }
}
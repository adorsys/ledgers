package de.adorsys.ledgers.middleware.impl.service;


import de.adorsys.ledgers.deposit.api.domain.AccountStatusBO;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String USER_LOGIN = "login";
    private static final String TPP_LOGIN = "tpp login";
    private static final String IBAN = "DE123456789";
    private static final String IBAN_NOT_OWNED = "DE987654321";
    private static final String ACCOUNT_ID = "account id";
    private static final Currency USD = Currency.getInstance("USD");
    @InjectMocks
    private AccessService service;

    @Mock
    private UserService userService;

    @Test
    public void updateAccountAccessNewAccount_new_access() {
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN);

        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, USER_LOGIN);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));
    }

    @Test
    public void updateAccountAccessNewAccount_existing_access() {
        UserBO initialUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(IBAN, EUR, 50))), USER_LOGIN);

        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, USER_LOGIN);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(1)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertThat(values.iterator().next().size() == 1).isTrue();
        assertThat(captor.getValue()).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));
    }

    @Test
    public void updateAccountAccessNewAccount_new_access_tpp_add() {
        UserBO initialUser = getUserBO(new ArrayList<>(), USER_LOGIN);
        UserBO tppUser = getUserBO(new ArrayList<>(singletonList(getAccessBO(IBAN, USD, 100))), TPP_LOGIN);
        when(userService.findByLogin(eq(TPP_LOGIN))).thenReturn(tppUser);

        service.updateAccountAccessNewAccount(getDepostAccountBO(), initialUser, TPP_LOGIN);

        ArgumentCaptor<List<AccountAccessBO>> captor = ArgumentCaptor.forClass(List.class);
        verify(userService, times(2)).updateAccountAccess(any(), captor.capture());
        List<List<AccountAccessBO>> values = captor.getAllValues();
        assertThat(values.size() == 2).isTrue();

        assertThat(values.get(0).size() == 1).isTrue();
        assertThat(values.get(0)).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100));

        assertThat(values.get(1).size() == 2).isTrue();
        assertThat(values.get(1)).containsExactlyInAnyOrder(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 100));
    }

    @Test
    public void filterOwnedAccounts() {
        List<String> result = service.filterOwnedAccounts(asList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER), getAccessTO(IBAN_NOT_OWNED, USD, AccessTypeTO.READ)));
        assertThat(result).containsExactlyInAnyOrder(IBAN);
    }

    @Test
    public void filterOwnedAccounts_null() {
        List<String> result = service.filterOwnedAccounts(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void resolveScaWeightByDebtorAccount() {
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN);
        assertThat(result).isEqualTo(100);
    }

    @Test
    public void resolveScaWeightByDebtorAccount_not_100() {
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 50));
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN);
        assertThat(result).isEqualTo(50);
    }

    @Test
    public void resolveScaWeightByDebtorAccount_not_owned() {
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN, USD, 50));
        int result = service.resolveScaWeightByDebtorAccount(accesses, IBAN_NOT_OWNED);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void resolveMinimalScaWeightForConsent() {
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(singletonList(IBAN)), accesses);
        assertThat(result).isEqualTo(100);
    }

    @Test
    public void resolveMinimalScaWeightForConsent_50() {
        List<AccountAccessBO> accesses = asList(getAccessBO(IBAN, EUR, 100), getAccessBO(IBAN_NOT_OWNED, USD, 50));
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(asList(IBAN, IBAN_NOT_OWNED)), accesses);
        assertThat(result).isEqualTo(50);
    }

    @Test
    public void resolveMinimalScaWeightForConsent_empty() {
        List<AccountAccessBO> accesses = new ArrayList<>();
        int result = service.resolveMinimalScaWeightForConsent(getAisAccess(asList(IBAN, IBAN_NOT_OWNED)), accesses);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void userHasAccessToAccount() {
        boolean result = service.userHasAccessToAccount(getUserTO(singletonList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER))), IBAN);
        assertThat(result).isTrue();
    }

    @Test
    public void userHasAccessToAccount_no_access() {
        boolean result = service.userHasAccessToAccount(getUserTO(singletonList(getAccessTO(IBAN, EUR, AccessTypeTO.OWNER))), IBAN_NOT_OWNED);
        assertThat(result).isFalse();
    }

    private AisAccountAccessInfoBO getAisAccess(List<String> ibans) {
        AisAccountAccessInfoBO ais = new AisAccountAccessInfoBO();
        ais.setAccounts(ibans);
        ais.setBalances(Collections.emptyList());
        ais.setTransactions(Collections.emptyList());
        return ais;
    }

    private UserBO getUserBO(List<AccountAccessBO> accesses, String login) {
        UserBO userBO = new UserBO(login, "email", "12345");
        userBO.setAccountAccesses(accesses);
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
        return new DepositAccountBO(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "name", "product", AccountTypeBO.CACC, AccountStatusBO.ENABLED, null, null, AccountUsageBO.PRIV, "details");
    }


}
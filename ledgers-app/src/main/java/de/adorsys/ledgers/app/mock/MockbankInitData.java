package de.adorsys.ledgers.app.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used to map mock bank initial data loading from yml files.
 *
 * @author fpo
 */
@Data
public class MockbankInitData extends BalancesData {
    private List<AccountDetailsTO> accounts = new ArrayList<>();
    private List<UserTO> users = new ArrayList<>();
    private List<SinglePaymentsData> singlePayments = new ArrayList<>();
    private List<BulkPaymentsData> bulkPayments = new ArrayList<>();
    private List<TransactionData> transactions = new ArrayList<>();
    private List<AccountBalance> balances = new ArrayList<>();

    @JsonIgnore
    public String getUserNameByIban(String iban) {
        return getFirstUser(iban)
                       .map(UserTO::getLogin)
                       .orElseThrow(() -> UserManagementModuleException.builder().build());
    }

    @JsonIgnore
    public String getUserIdByIban(String iban) {
        return getFirstUser(iban)
                .map(UserTO::getId)
                .orElseThrow(() -> UserManagementModuleException.builder().build());
    }

    private Optional<UserTO> getFirstUser(String iban) {
        return users.stream()
                .filter(u -> u.hasAccessToAccountWithIban(iban))
                .findFirst();
    }


    @JsonIgnore
    public List<String> getUserIdByIban(String iban, String excludedUserId) {
        return users.stream()
                .filter(u -> !u.getId().equals(excludedUserId))
                .filter(u -> u.hasAccessToAccountWithIban(iban))
                .map(UserTO::getId)
                .collect(Collectors.toList());

    }

    @JsonIgnore
    public Optional<AccountAccessTO> getAccountAccess(String iban, String userId) {
        return users.stream()
                .filter(u -> !u.getId().equals(userId))
                .filter(u -> u.hasAccessToAccountWithIban(iban))
                .flatMap(u -> u.getAccountAccesses().stream())
                .filter(a -> a.getIban().equals(iban))
                .findFirst();

    }
}

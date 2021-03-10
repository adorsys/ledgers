package de.adorsys.ledgers.app.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
}

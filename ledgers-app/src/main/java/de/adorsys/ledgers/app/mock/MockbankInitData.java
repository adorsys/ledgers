package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
}

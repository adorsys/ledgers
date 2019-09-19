package de.adorsys.ledgers.middleware.api.domain.um;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedDataTO {
    private List<UserTO> users = new ArrayList<>();
    private Map<String, AccountDetailsTO> details = new HashMap<>();
    private Map<String, AccountBalanceTO> balances = new HashMap<>();
    private List<SinglePaymentTO> payments = new ArrayList<>();
    private boolean generatePayments;
    private String branch;
}
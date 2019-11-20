package de.adorsys.ledgers.deposit.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static de.adorsys.ledgers.deposit.api.domain.AccountStatusBO.ENABLED;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccountDetailsBO {
    private DepositAccountBO account;
    private List<BalanceBO> balances = new ArrayList<>();

    public boolean isEnabled() {
        return account.getAccountStatus() == ENABLED;
    }
}

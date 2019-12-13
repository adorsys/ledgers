package de.adorsys.ledgers.deposit.api.service.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ASPSPConfigData {
    private String name;
    private String ledger;
    private String coaFile;
    private String depositParentAccount;
    private List<LedgerAccountModel> coaExtensions = new ArrayList<>();
    private List<ClearingAccount> clearingAccounts = new ArrayList<>();
    private String cashAccount;

    /*Account number present means config is up to date*/
    private String updateMarkerAccountNbr;

    public String getClearingAccount(String paymentProduct) {
        return clearingAccounts.stream().filter(a -> a.getPaymentProduct().equalsIgnoreCase(paymentProduct))
                       .map(ClearingAccount::getAccountNbr)
                       .findFirst()
                       .orElseGet(() -> getClearingAccount("others"));
    }
}

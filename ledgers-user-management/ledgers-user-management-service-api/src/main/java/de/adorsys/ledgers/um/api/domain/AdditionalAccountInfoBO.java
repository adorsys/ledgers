package de.adorsys.ledgers.um.api.domain;

import lombok.Data;

@Data
public class AdditionalAccountInfoBO {
    private String accountOwnerName;
    private Integer scaWeight;

    public AdditionalAccountInfoBO(UserBO user) {
        this.accountOwnerName = user.getLogin();
        this.scaWeight = user.getAccountAccesses().iterator().next().getScaWeight();
    }
}

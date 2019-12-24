package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;
import java.util.EnumSet;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountAccessTO {
    private String id;
    @NotNull
    private String iban;
    @NotNull
    private Currency currency;
    @NotNull
    private AccessTypeTO accessType;
    @NotNull
    private int scaWeight;
    @NotNull
    private String accountId;

    @JsonIgnore
    public boolean hasPaymentAccess(String requestedIban) {
        return hasIban(requestedIban) &&
                       EnumSet.of(AccessTypeTO.OWNER, AccessTypeTO.DISPOSE).contains(accessType);
    }

    @JsonIgnore
    public boolean hasIban(String requestedIban) {
        return equalsIgnoreCase(requestedIban, iban);
    }
}

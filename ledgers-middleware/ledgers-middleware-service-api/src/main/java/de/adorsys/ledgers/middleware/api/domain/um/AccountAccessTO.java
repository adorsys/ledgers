package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Data
public class AccountAccessTO {
    private String id;
    @NotNull
    private String iban;
    @NotNull
    private AccessTypeTO accessType;
    @NotNull
    private int scaWeight;

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

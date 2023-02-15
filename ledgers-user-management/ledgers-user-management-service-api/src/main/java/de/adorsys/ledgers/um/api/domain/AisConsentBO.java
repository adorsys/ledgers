/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AisConsentBO {
    private String id;
    private String userId;
    private String tppId;
    private int frequencyPerDay;
    private AisAccountAccessInfoBO access;
    private LocalDate validUntil;
    private boolean recurringIndicator;

    public Set<String> getUniqueIbans() {
        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                       .filter(Objects::nonNull)
                       .flatMap(Collection::stream)
                       .collect(Collectors.toSet());
    }
}

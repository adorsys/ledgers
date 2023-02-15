/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


// TODO refactor it https://git.adorsys.de/adorsys/xs2a/ledgers/issues/236
public enum AccountIdentifierTypeBO {
    ACCOUNT_ID {
        @Override
        public List<AdditionalAccountInfoBO> getAdditionalAccountInfo(String accountId, Function<String, List<UserBO>> usersByIban, Function<String, List<UserBO>> usersByAccountId) {
            return usersByAccountId.apply(accountId).stream()
                           .map(AdditionalAccountInfoBO::new)
                           .collect(Collectors.toList());
        }
    },
    IBAN {
        @Override
        public List<AdditionalAccountInfoBO> getAdditionalAccountInfo(String accountId, Function<String, List<UserBO>> usersByIban, Function<String, List<UserBO>> usersByAccountId) {
            return usersByIban.apply(accountId).stream()
                           .map(AdditionalAccountInfoBO::new)
                           .collect(Collectors.toList());
        }
    };

    public abstract List<AdditionalAccountInfoBO> getAdditionalAccountInfo(String accountId, Function<String, List<UserBO>> usersByIban, Function<String, List<UserBO>> usersByAccountId);
}

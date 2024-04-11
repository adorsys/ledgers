/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AisAccountAccessInfoBO {

    private List<String> accounts;
    private List<String> balances;
    private List<String> transactions;
    private AisAccountAccessTypeBO availableAccounts;
    private AisAccountAccessTypeBO allPsd2;
}

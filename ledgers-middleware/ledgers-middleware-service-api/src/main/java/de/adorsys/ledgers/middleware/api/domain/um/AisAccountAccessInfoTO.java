/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Ais account access information", name = "AisAccountAccessInfo")
public class AisAccountAccessInfoTO {

    @Schema(description = "Access to accounts")
    private List<String> accounts;

    @Schema(description = "Access to balances")
    private List<String> balances;

    @Schema(description = "Access to transactions")
    private List<String> transactions;

    @Schema(description = "Consent on all available accounts of psu", example = "ALL_ACCOUNTS")
    private AisAccountAccessTypeTO availableAccounts;

    @Schema(description = "Consent on all accounts, balances and transactions of psu", example = "ALL_ACCOUNTS")
    private AisAccountAccessTypeTO allPsd2;

    @JsonIgnore
    public Set<String> getListedAccountsIbans() {
        return Stream.concat(
                Stream.concat(
                        checkAndTransform(this.accounts),
                        checkAndTransform(this.balances)),
                checkAndTransform(this.transactions))
                       .collect(Collectors.toSet());
    }

    private Stream<String> checkAndTransform(List<String> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            return collection.stream();
        }
        return Stream.of();
    }
}

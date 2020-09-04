/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

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

    public boolean hasIbanInAccess(String iban) {
        return availableAccounts != null ||
                       allPsd2 != null ||
                       accounts != null && accounts.contains(iban) ||
                       balances != null && balances.contains(iban) ||
                       transactions != null && transactions.contains(iban);
    }

    public Set<String> getListedAccounts() {
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

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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Ais account access information", value = "AisAccountAccessInfo")
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
}

/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthConfirmationTO {
    private boolean partiallyAuthorised;
    private boolean multilevelScaRequired;
    private TransactionStatusTO transactionStatus;
    private boolean success;

    public AuthConfirmationTO partiallyAuthorised(boolean partiallyAuthorised) {
        this.partiallyAuthorised = partiallyAuthorised;
        return this;
    }

    public AuthConfirmationTO multilevelScaRequired(boolean multilevelScaRequired) {
        this.multilevelScaRequired = multilevelScaRequired;
        return this;
    }

    public AuthConfirmationTO transactionStatus(TransactionStatusTO transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    public AuthConfirmationTO success(boolean success) {
        this.success = success;
        return this;
    }
}

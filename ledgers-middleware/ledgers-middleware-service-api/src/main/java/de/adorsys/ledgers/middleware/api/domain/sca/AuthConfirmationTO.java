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
}

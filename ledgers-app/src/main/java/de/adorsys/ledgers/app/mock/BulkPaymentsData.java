/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import lombok.Data;

@Data
public class BulkPaymentsData extends BalancesData {
    private PaymentTO bulkPayment;
}

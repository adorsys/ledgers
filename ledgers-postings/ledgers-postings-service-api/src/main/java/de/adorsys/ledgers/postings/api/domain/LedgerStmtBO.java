/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import lombok.Data;

/**
 * Document the state of a ledger at the statement date.
 * 
 * @author fpo
 *
 */
@Data
public class LedgerStmtBO extends FinancialStmtBO {
	/* Name of the containing ledger */
	private LedgerBO ledger;
}

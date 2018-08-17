package de.adorsys.ledgers.postingserver.controller;

import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import lombok.Data;

@Data
public class NewLedgerAccountTypeRequest {
	LedgerAccountType parent;
	String name;
	String desc;
}

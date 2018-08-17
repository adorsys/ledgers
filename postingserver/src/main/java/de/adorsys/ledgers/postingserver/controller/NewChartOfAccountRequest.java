package de.adorsys.ledgers.postingserver.controller;

import java.util.List;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import lombok.Data;

@Data
public class NewChartOfAccountRequest {

	private ChartOfAccount chartOfAccount;
	
	private List<String> rootAccountTypes;
}

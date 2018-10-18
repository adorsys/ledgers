package de.adorsys.ledgers.postings.exception;

import de.adorsys.ledgers.postings.domain.ChartOfAccountBO;

public class ChartOfAccountNotFoundException extends Exception {
	private static final long serialVersionUID = -1713219984198663520L;

	public ChartOfAccountNotFoundException(String message) {
		super(message);
	}

	public ChartOfAccountNotFoundException(ChartOfAccountBO model) {
		this(String.format("Entity of type %s and id %s not found.", model.getClass().getName(), model.getId()));
	}
}

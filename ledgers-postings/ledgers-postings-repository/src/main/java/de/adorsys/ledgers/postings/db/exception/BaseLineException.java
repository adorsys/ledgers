package de.adorsys.ledgers.postings.db.exception;

import java.time.LocalDateTime;

public class BaseLineException extends Exception {

	public BaseLineException(LocalDateTime pstTime, LocalDateTime accStmtTime) {
		super(String.format("posting time %s is before the last ledger closing %s", pstTime, accStmtTime));
	}

}

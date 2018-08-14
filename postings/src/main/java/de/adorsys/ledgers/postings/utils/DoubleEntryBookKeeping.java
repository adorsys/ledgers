package de.adorsys.ledgers.postings.utils;

import java.math.BigDecimal;
import java.util.List;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingLine;

public class DoubleEntryBookKeeping {

	public static void validate(Posting posting){
		List<PostingLine> lines = posting.getLines();
		BigDecimal sumDebit = BigDecimal.ZERO;
		BigDecimal sumCredit = BigDecimal.ZERO;
		for (PostingLine line : lines) {
			switch (line.getSide()) {
			case C:
				sumCredit = sumCredit.add(line.getAmount());
				break;
			case D:
				sumDebit = sumDebit.add(line.getAmount());
				break;
			default:
				throw new IllegalArgumentException(String.format("Unknown jorunal side %s", line.getSide()));
			}
		}
		
		if(!sumDebit.equals(sumCredit))
			throw new IllegalArgumentException(String.format("Debit summs up to %s while credit sums up to %s", sumDebit, sumCredit));
			
	}
}

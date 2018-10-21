package de.adorsys.ledgers.postings.utils;

import java.math.BigDecimal;
import java.util.List;

import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;

public class DoubleEntryBookKeeping {

    public static void validate(Posting posting) {
        List<PostingLine> lines = posting.getLines();
        BigDecimal sumDebit = BigDecimal.ZERO;
        BigDecimal sumCredit = BigDecimal.ZERO;
        for (PostingLine line : lines) {
            sumDebit = sumDebit.add(line.getDebitAmount());
            sumCredit = sumCredit.add(line.getCreditAmount());
        }

        if (!sumDebit.equals(sumCredit)) {
            throw new IllegalArgumentException(String.format("Debit summs up to %s while credit sums up to %s", sumDebit, sumCredit));
        }
    }
}

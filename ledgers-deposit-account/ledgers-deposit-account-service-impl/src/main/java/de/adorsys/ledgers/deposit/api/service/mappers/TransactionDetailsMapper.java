package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.util.SerializationUtils;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class TransactionDetailsMapper {
    public TransactionDetailsBO toTransaction(PostingLineBO pl) {
        if (pl.getDetails() == null) {
            return new TransactionDetailsBO();
        }
        return SerializationUtils.readValueFromString(pl.getDetails(), TransactionDetailsBO.class);
    }

    /**
     * Produces a signed transaction detail object.
     * 
     * @param pl posting line
     * @return converted TransactionDetailsBO object
     */
	public TransactionDetailsBO toTransactionSigned(PostingLineBO pl) {
		TransactionDetailsBO transaction = toTransaction(pl);
		if(BigDecimal.ZERO.compareTo(pl.getCreditAmount())==0){
			AmountBO transactionAmount = transaction.getTransactionAmount();
			transactionAmount.setAmount(transactionAmount.getAmount().negate());
		}
		return transaction;
	}
}

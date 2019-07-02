package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransactionDetailsMapper {
    private final ObjectMapper objectMapper;

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

	private TransactionDetailsBO toTransaction(PostingLineBO pl) {
		if (pl.getDetails() == null) {
			return new TransactionDetailsBO();
		}
		try {
			return objectMapper.readValue(pl.getDetails(), TransactionDetailsBO.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}

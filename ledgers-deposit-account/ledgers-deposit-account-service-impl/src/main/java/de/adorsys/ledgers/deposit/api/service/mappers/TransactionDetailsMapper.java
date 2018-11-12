package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.util.SerializationUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionDetailsMapper {
    public TransactionDetailsBO toTransaction(PostingLineBO pl) {
        if (pl.getDetails() == null) {
            return new TransactionDetailsBO();
        }
        return SerializationUtils.readValueFromString(pl.getDetails(), TransactionDetailsBO.class);
    }
}

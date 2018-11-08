package de.adorsys.ledgers.deposit.api.service.mappers;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.util.SerializationUtils;

@SuppressWarnings("PMD")
@Mapper(componentModel = "spring")
public class TransactionDetailsMapper {
	public TransactionDetailsBO toTransaction(PostingLineBO pl ) {
		return SerializationUtils.readValueFromString(pl.getDetails(), TransactionDetailsBO.class);
	}
}

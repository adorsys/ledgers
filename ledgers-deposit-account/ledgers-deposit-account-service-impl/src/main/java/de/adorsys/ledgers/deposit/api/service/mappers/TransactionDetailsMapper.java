package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        if (BigDecimal.ZERO.compareTo(pl.getCreditAmount()) == 0) {
            AmountBO transactionAmount = transaction.getTransactionAmount();
            transactionAmount.setAmount(transactionAmount.getAmount().negate());
            transaction.setCreditorId(null);
            reverse(transaction::setCreditorAccount, transaction::setDebtorAccount, transaction::getCreditorAccount, transaction::getDebtorAccount);
            reverse(transaction::setCreditorAgent, transaction::setDebtorAgent, transaction::getCreditorAgent, transaction::getDebtorAgent);
            reverse(transaction::setCreditorName, transaction::setDebtorName, transaction::getCreditorName, transaction::getDebtorName);
            reverse(transaction::setUltimateCreditor, transaction::setUltimateDebtor, transaction::getUltimateCreditor, transaction::getUltimateDebtor);
        }
        return transaction;
    }

    private <T> void reverse(Consumer<T> consumer1, Consumer<T> consumer2, Supplier<T> supplier1, Supplier<T> supplier2) {
        T temp = supplier1.get();
        consumer1.accept(supplier2.get());
        consumer2.accept(temp);
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

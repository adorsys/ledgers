package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.MockBookingDetails;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    Map<String, String> bookMockTransaction(List<MockBookingDetails> trDetails);
}

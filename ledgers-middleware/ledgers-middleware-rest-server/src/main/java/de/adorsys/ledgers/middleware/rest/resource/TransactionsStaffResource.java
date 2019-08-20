package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.deposit.api.domain.MockBookingDetails;
import de.adorsys.ledgers.deposit.api.service.TransactionService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
public class TransactionsStaffResource implements TransactionsStaffResourceAPI {
    private final TransactionService transactionService;

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Map<String, String>> transactions(List<MockBookingDetails> data) {
        return new ResponseEntity<>(transactionService.bookMockTransaction(data), HttpStatus.CREATED);
    }
}

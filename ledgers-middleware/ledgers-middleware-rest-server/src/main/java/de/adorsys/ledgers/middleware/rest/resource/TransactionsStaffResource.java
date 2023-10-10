/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.deposit.api.domain.MockBookingDetailsBO;
import de.adorsys.ledgers.deposit.api.service.TransactionService;
import de.adorsys.ledgers.middleware.api.domain.account.MockBookingDetails;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.mapper.MockTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(TransactionsStaffResourceAPI.BASE_PATH)
public class TransactionsStaffResource implements TransactionsStaffResourceAPI {
    private final TransactionService transactionService;
    private final MockTransactionMapper transactionMapper;

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<Map<String, String>> transactions(List<MockBookingDetails> data) {
        List<MockBookingDetailsBO> dataBO = transactionMapper.toMockTransactionDetailsBO(data);
        return new ResponseEntity<>(transactionService.bookMockTransaction(dataBO), HttpStatus.CREATED);
    }
}

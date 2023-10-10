/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReportTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping("/staff-access" + AccountRestAPI.BASE_PATH)
public class AccountMgmStaffResource implements AccountMgmStaffResourceAPI {
    private final MiddlewareAccountManagementService middlewareAccountService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountIban(#iban)")
    public ResponseEntity<List<AccountDetailsTO>> getAccountsByIbanAndCurrency(String iban, String currency) {
        return ResponseEntity.ok(middlewareAccountService.getAccountsByIbanAndCurrency(iban, currency));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToUser(#userId)")
    public ResponseEntity<Boolean> createDepositAccountForUser(String userId, AccountDetailsTO accountDetailsTO) {
        boolean created = middlewareAccountService.createDepositAccount(userId, scaInfoHolder.getScaInfo(), accountDetailsTO);
        return ResponseEntity.ok(created);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccounts() {
        return ResponseEntity.ok(middlewareAccountService.listDepositAccountsByBranch(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<CustomPageImpl<AccountDetailsTO>> getListOfAccountsPaged(String queryParam, int page, int size, boolean withBalance) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        CustomPageImpl<AccountDetailsTO> details = middlewareAccountService.listDepositAccountsByBranchPaged(scaInfoHolder.getUserId(), queryParam, withBalance, pageable);
        return ResponseEntity.ok(details);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountId(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(String accountId) {
        return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountId(#accountId) && @accountAccessSecurityFilter.isEnabledAccount(#accountId)")
    public ResponseEntity<Void> depositCash(String accountId, AmountTO amount) {
        middlewareAccountService.depositCash(scaInfoHolder.getScaInfo(), accountId, amount);
        return ResponseEntity.accepted().build();
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountId(#accountId)")
    public ResponseEntity<AccountReportTO> getExtendedAccountDetailsById(String accountId) {
        long start = System.nanoTime();
        AccountReportTO accountReport = middlewareAccountService.getAccountReport(accountId);
        log.info("Loaded report in {} seconds", TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
        return ResponseEntity.ok(accountReport);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountId(#accountId)")
    public ResponseEntity<Boolean> changeStatus(String accountId) {
        return ResponseEntity.ok(middlewareAccountService.changeStatus(accountId, false));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToAccountId(#accountId)")
    public ResponseEntity<Void> changeCreditLimit(String accountId, BigDecimal creditLimit) {
        middlewareAccountService.changeCreditLimit(accountId, creditLimit);
        return ResponseEntity.accepted().build();
    }
}

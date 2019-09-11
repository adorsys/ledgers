package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@MiddlewareResetResource
@RequiredArgsConstructor
@RequestMapping(ResetDataMgmtStaffAPI.BASE_PATH)
public class ResetDataMgmtStaffResource implements ResetDataMgmtStaffAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareAccountManagementService accountManagementService;
    private final AppManagementService appManagementService;

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> account(String iban) {
        accountManagementService.deleteTransactions(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), iban);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> branch(String branchId) {
        appManagementService.removeBranch(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), branchId);
        return ResponseEntity.ok().build();
    }
}

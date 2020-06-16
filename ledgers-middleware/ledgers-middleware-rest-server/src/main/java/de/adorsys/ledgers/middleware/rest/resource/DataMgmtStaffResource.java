package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.List;
import java.util.Set;

@RestController
@MiddlewareResetResource
@RequiredArgsConstructor
@RequestMapping(DataMgmtStaffAPI.BASE_PATH)
public class DataMgmtStaffResource implements DataMgmtStaffAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareAccountManagementService accountManagementService;
    private final AppManagementService appManagementService;
    private final CurrencyService currencyService;
    private final MiddlewareRecoveryService recoveryService;

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> account(String accountId) {
        accountManagementService.deleteTransactions(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), accountId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> depositAccount(String accountId) {
        accountManagementService.deleteAccount(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), accountId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> user(String userId) {
        accountManagementService.deleteUser(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), userId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> branch(String branchId) {
        appManagementService.removeBranch(scaInfoHolder.getUserId(), scaInfoHolder.getScaInfo().getUserRole(), branchId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> uploadData(UploadedDataTO data) {
        appManagementService.uploadData(data, scaInfoHolder.getScaInfo());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Set<Currency>> currencies() {
        return ResponseEntity.ok(currencyService.getSupportedCurrencies());
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> branchId(BbanStructure bbanStructure) {
        return ResponseEntity.ok(appManagementService.generateNextBban(bbanStructure));
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> createPoint(RecoveryPointTO recoveryPoint) {
        recoveryService.createRecoveryPoint(scaInfoHolder.getUserId(), recoveryPoint);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RecoveryPointTO>> getAllPoints() {
        return ResponseEntity.ok(recoveryService.getAll(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<RecoveryPointTO> getPoint(Long id) {
        return ResponseEntity.ok(recoveryService.getPointById(scaInfoHolder.getUserId(), id));
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> deletePoint(Long id) {
        recoveryService.deleteById(scaInfoHolder.getUserId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

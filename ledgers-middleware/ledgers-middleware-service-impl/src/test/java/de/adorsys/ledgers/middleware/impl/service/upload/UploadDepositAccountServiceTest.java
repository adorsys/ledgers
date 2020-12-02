package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadDepositAccountServiceTest {

    @InjectMocks
    private UploadDepositAccountService service;

    @Mock
    private MiddlewareAccountManagementService middlewareAccountService;

    @Test
    void uploadDepositAccounts() {
        UserTO user = new UserTO();
        AccountAccessTO access = new AccountAccessTO();
        access.setIban("iban");
        user.setAccountAccesses(List.of(access));
        service.uploadDepositAccounts(List.of(user), Map.of("iban", new AccountDetailsTO()), new ScaInfoTO());
        verify(middlewareAccountService, times(1)).createDepositAccount(any(), any(), any());
    }

    @Test
    void uploadDepositAccounts_exception() {
        doThrow(DepositModuleException.class).when(middlewareAccountService).createDepositAccount(any(), any(), any());
        UserTO user = new UserTO();
        AccountAccessTO access = new AccountAccessTO();
        access.setIban("iban");
        user.setAccountAccesses(List.of(access));
        service.uploadDepositAccounts(List.of(user), Map.of("iban", new AccountDetailsTO()), new ScaInfoTO());
        verify(middlewareAccountService, times(1)).createDepositAccount(any(), any(), any());
    }
}
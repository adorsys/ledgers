package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountResourceTest {

    @InjectMocks
    AccountResource accountResource;

    @Mock
    MiddlewareAccountManagementService middlewareAccountService;

    @Test
    void fundsConfirmation_zero() {
        FundsConfirmationRequestTO request = getFundsConfirmationRequest(BigDecimal.ZERO);
        // Then
        assertThrows(MiddlewareModuleException.class, () -> accountResource.fundsConfirmation(request));
    }

    @Test
    void fundsConfirmation_negative() {
        FundsConfirmationRequestTO request = getFundsConfirmationRequest(BigDecimal.valueOf(-100));
        // Then
        assertThrows(MiddlewareModuleException.class, () -> accountResource.fundsConfirmation(request));
    }

    @Test()
    void fundsConfirmation_positive() {
        // Given
        when(middlewareAccountService.confirmFundsAvailability(any())).thenReturn(true);

        // When
        ResponseEntity<Boolean> confirmation = accountResource.fundsConfirmation(getFundsConfirmationRequest(BigDecimal.TEN));

        // Then
        assertTrue(confirmation.getStatusCode().is2xxSuccessful());
        assertTrue(confirmation.getBody());
    }

    private FundsConfirmationRequestTO getFundsConfirmationRequest(BigDecimal amount) {
        return new FundsConfirmationRequestTO("PSU_ID", new AccountReferenceTO(), new AmountTO(Currency.getInstance("EUR"), amount), null, null);
    }
}

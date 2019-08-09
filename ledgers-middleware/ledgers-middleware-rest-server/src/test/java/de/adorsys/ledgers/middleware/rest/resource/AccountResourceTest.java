package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountResourceTest {

    @InjectMocks
    AccountResource accountResource;

    @Mock
    MiddlewareAccountManagementService middlewareAccountService;

    @Test(expected = MiddlewareModuleException.class)
    public void fundsConfirmation_zero() {
        accountResource.fundsConfirmation(getFundsConfirmationRequest(BigDecimal.ZERO));
    }

    @Test(expected = MiddlewareModuleException.class)
    public void fundsConfirmation_negative() {
        accountResource.fundsConfirmation(getFundsConfirmationRequest(BigDecimal.valueOf(-100)));
    }

    @Test()
    public void fundsConfirmation_positive() {
        when(middlewareAccountService.confirmFundsAvailability(any())).thenReturn(true);
        ResponseEntity<Boolean> confirmation = accountResource.fundsConfirmation(getFundsConfirmationRequest(BigDecimal.TEN));
        assertThat(confirmation.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(confirmation.getBody()).isTrue();
    }

    private FundsConfirmationRequestTO getFundsConfirmationRequest(BigDecimal amount) {
        return new FundsConfirmationRequestTO("PSU_ID",new AccountReferenceTO(),new AmountTO(Currency.getInstance("EUR"), amount),null,null);
    }
}

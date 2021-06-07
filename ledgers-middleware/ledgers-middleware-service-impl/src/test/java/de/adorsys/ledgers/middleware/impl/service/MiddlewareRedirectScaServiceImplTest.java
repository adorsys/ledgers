package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseConverter;
import de.adorsys.ledgers.middleware.impl.service.message.PsuMessageResolver;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareRedirectScaServiceImplTest {

    @InjectMocks
    MiddlewareRedirectScaServiceImpl service;

    @Mock
    private UserService userService;
    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private ScaResponseConverter scaResponseConverter;
    @Mock
    private AccessService accessService;
    @Mock
    private BearerTokenMapper bearerTokenMapper;
    @Mock
    private PsuMessageResolver messageResolver;

    private final UserBO user = getUser();

    private UserBO getUser() {
        UserBO user = new UserBO();
        user.setScaUserData(List.of(new ScaUserDataBO()));
        return user;
    }

    @Test
    void startScaOperation() {
        when(userService.findByLogin(any())).thenReturn(user);
        when(scaOperationService.checkIfExistsOrNew(any())).thenReturn(new SCAOperationBO());
        when(scaResponseConverter.mapResponse(any(), any(), any(), any(), anyInt(), any())).thenReturn(new GlobalScaResponseTO());
        when(paymentService.getPaymentById(any())).thenReturn(new PaymentBO());

        GlobalScaResponseTO result = service.startScaOperation(new StartScaOprTO("1", OpTypeTO.PAYMENT), new ScaInfoTO());
        assertNotNull(result);
        verify(scaOperationService, times(1)).checkIfExistsOrNew(any());
        verify(scaResponseConverter, times(1)).mapResponse(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void startScaOperation_error_no_sca_methods() {
        when(userService.findByLogin(any())).thenReturn(new UserBO());
        StartScaOprTO opr = new StartScaOprTO("1", OpTypeTO.PAYMENT);
        ScaInfoTO info = new ScaInfoTO();
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.startScaOperation(opr, info));
        assertEquals(MiddlewareErrorCode.SCA_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void getMethods() {
        when(userService.findByLogin(any())).thenReturn(user);
        when(scaResponseConverter.mapResponse(any(), any(), any(), any(), anyInt(), any())).thenReturn(new GlobalScaResponseTO());
        GlobalScaResponseTO result = service.getMethods("authId", new ScaInfoTO());
        assertNotNull(result);
        verify(scaOperationService, times(1)).loadAuthCode(any());
        verify(scaResponseConverter, times(1)).mapResponse(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void selectMethod() {
        when(scaOperationService.loadAuthCode(any())).thenReturn(new SCAOperationBO());
        when(userService.findByLogin(any())).thenReturn(user);
        when(paymentService.getPaymentById(any())).thenReturn(new PaymentBO());
        when(scaResponseConverter.mapResponse(any(), any(), any(), any(), anyInt(), any())).thenReturn(new GlobalScaResponseTO());

        GlobalScaResponseTO result = service.selectMethod(new ScaInfoTO());
        assertNotNull(result);
        verify(scaOperationService, times(1)).loadAuthCode(any());
        verify(paymentService, times(1)).getPaymentById(any());
        verify(scaOperationService, times(1)).generateAuthCode(any(), any(), eq(ScaStatusBO.SCAMETHODSELECTED));
        verify(scaResponseConverter, times(1)).mapResponse(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void selectMethod_consent() {
        SCAOperationBO scaOperationBO = new SCAOperationBO();
        scaOperationBO.setOpType(OpTypeBO.CONSENT);
        when(scaOperationService.loadAuthCode(any())).thenReturn(scaOperationBO);
        when(userService.findByLogin(any())).thenReturn(user);
        AisConsentBO aisConsentBO = new AisConsentBO() {
            @Override
            public Set<String> getUniqueIbans() {
                return Set.of("123456789");
            }
        };
        when(userService.loadConsent(any())).thenReturn(aisConsentBO);
        when(scaResponseConverter.mapResponse(any(), any(), any(), any(), anyInt(), any())).thenReturn(new GlobalScaResponseTO());

        GlobalScaResponseTO result = service.selectMethod(new ScaInfoTO());
        assertNotNull(result);
        verify(scaOperationService, times(1)).loadAuthCode(any());
        verify(scaOperationService, times(1)).generateAuthCode(any(), any(), eq(ScaStatusBO.SCAMETHODSELECTED));
        verify(scaResponseConverter, times(1)).mapResponse(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void confirmAuthorization() {
        when(scaOperationService.loadAuthCode(any())).thenReturn(new SCAOperationBO());
        when(userService.findByLogin(any())).thenReturn(user);
        when(paymentService.getPaymentById(any())).thenReturn(new PaymentBO());
        when(scaOperationService.validateAuthCode(any(), any(), any(), anyInt())).thenReturn(new ScaValidationBO(true));
        when(scaOperationService.authenticationCompleted(any(), any())).thenReturn(true);
        when(scaResponseConverter.mapResponse(any(), any(), any(), any(), anyInt(), any())).thenReturn(new GlobalScaResponseTO());

        GlobalScaResponseTO result = service.confirmAuthorization(new ScaInfoTO());
        assertNotNull(result);
        verify(scaOperationService, times(1)).validateAuthCode(any(), any(), any(), anyInt());
        verify(scaOperationService, times(1)).authenticationCompleted(any(), any());
        verify(accessService, times(1)).exchangeTokenEndSca(anyBoolean(), any());
        verify(scaResponseConverter, times(1)).mapResponse(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void loadScaInformation() {
        SCAOperationBO operation = new SCAOperationBO();
        operation.setOpType(OpTypeBO.PAYMENT);
        operation.setOpId("opId");
        when(scaOperationService.loadAuthCode(any())).thenReturn(operation);
        StartScaOprTO result = service.loadScaInformation(any());
        assertNotNull(result);
        assertEquals(new StartScaOprTO("opId", OpTypeTO.PAYMENT), result);
    }
}
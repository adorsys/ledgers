package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseResolver;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicyHelper;
import de.adorsys.ledgers.middleware.impl.sca.AbstractScaChallengeData;
import de.adorsys.ledgers.middleware.impl.sca.EmailScaChallengeData;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSP;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewarePaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String USER_ID = "kjk345knkj45";
    private static final String SCA_ID = "scaId";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String AUTH_CODE = "123456";
    private static final String AUTHORISATION_ID = "authorisationId";
    private static final String USER_LOGIN = "userLogin";
    private static final String IBAN = "DE1234567890";

    @InjectMocks
    private MiddlewarePaymentServiceImpl middlewareService;

    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private SCAOperationService operationService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private SCAUtils scaUtils;
    @Mock
    private PaymentCoreDataPolicy coreDataPolicy;
    @Mock
    private AccessService accessService;
    @Mock
    private ScaResponseResolver scaResponseResolver;
    @Mock
    private PaymentSupportService supportService;
    @Mock
    private PageMapper pageMapper;

    private final ScaResponseResolver localResolver = new ScaResponseResolver(scaUtils, new ScaChallengeDataResolverImpl<AbstractScaChallengeData>(Collections.singletonList(new EmailScaChallengeData())), Mappers.getMapper(UserMapper.class));

    private final PaymentConverter pmtMapper = Mappers.getMapper(PaymentConverter.class);
    private static final PaymentTO PAYMENT_TO = readYml(PaymentTO.class, SINGLE_BO);
    private static final PaymentBO PAYMENT_BO = readYml(PaymentBO.class, SINGLE_BO);
    private static final ScaInfoTO SCA_INFO_TO = buildScaInfoTO();

    @Test
    void getPaymentStatusById() {
        // Given
        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        // When
        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        // Then
        assertEquals(TransactionStatusBO.RJCT.getName(), paymentResult.getName());
        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test
    void getPaymentStatusByIdWithException() {
        // Given
        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getPaymentStatusById(PAYMENT_ID));
        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test
    void getPaymentById() {
        // Given
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(PAYMENT_BO);
        when(paymentConverter.toPaymentTO(any())).thenReturn(PAYMENT_TO);

        // When
        PaymentTO result = middlewareService.getPaymentById(PAYMENT_ID);

        // Then
        assertNotNull(result);
    }

    @Test
    void getPaymentById_Fail_wrong_id() {
        // Given
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getPaymentById(WRONG_PAYMENT_ID));
    }

    @Test
    void execute_Payment_For_Any_Type_Of_Sca_Users() {
        // Given
        when(paymentService.getPaymentById(any())).thenReturn(PAYMENT_BO);
        when(paymentService.updatePaymentStatus(PAYMENT_ID, ACTC)).thenReturn(ACTC);
        when(paymentService.executePayment(any(), any())).thenReturn(ACTC);

        // When
        SCAPaymentResponseTO response = middlewareService.executePayment(SCA_INFO_TO, PAYMENT_ID);

        // Then
        assertNotNull(response);
        assertEquals(TransactionStatusTO.ACTC, response.getTransactionStatus());
    }

    @Test
    void initiatePayment() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        DepositAccountBO account = getAccountBO(paymentBO);
        when(supportService.getCheckedAccount(any())).thenReturn(account);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_LOGIN)).thenReturn(userBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(accessService.exchangeTokenStartSca(anyBoolean(), any())).thenReturn(new BearerTokenTO());
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);

        // When
        Object result = middlewareService.initiatePayment(SCA_INFO_TO, PAYMENT_TO, PaymentTypeTO.SINGLE);

        // Then
        assertNotNull(result);
    }

    private DepositAccountBO getAccountBO(PaymentBO paymentBO) {
        DepositAccountBO account = new DepositAccountBO();
        account.setId("");
        account.setIban(paymentBO.getDebtorAccount().getIban());
        return account;
    }

    @Test
    void executePayment_Success() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(operationService.authenticationCompleted(PAYMENT_ID, OpTypeBO.PAYMENT)).thenReturn(Boolean.TRUE);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_LOGIN)).thenReturn(userBO);
        when(paymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACSC);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));

        // When
        SCAPaymentResponseTO scaPaymentResponseTO = middlewareService.authorizePayment(SCA_INFO_TO, PAYMENT_ID);

        // Then
        assertNotNull(scaPaymentResponseTO);
    }

    @Test
    void initiatePaymentCancellation() {
        // Given
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(paymentService.getPaymentById(any())).thenReturn(paymentBO);
        when(scaUtils.userBO(USER_LOGIN)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));

        // When
        SCAPaymentResponseTO initiatePaymentCancellation = middlewareService.initiatePaymentCancellation(SCA_INFO_TO, PAYMENT_ID);

        // Then
        assertNotNull(initiatePaymentCancellation);
    }

    @Test
    void initiatePaymentCancellation_Failure_user_NF() {
        // Given
        when(scaUtils.userBO(USER_LOGIN)).thenThrow(MiddlewareModuleException.class);

        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePaymentCancellation(SCA_INFO_TO, PAYMENT_ID));
    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_NF() {
        // Given
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.initiatePaymentCancellation(SCA_INFO_TO, PAYMENT_ID));
    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() {
        // Given
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.initiatePaymentCancellation(SCA_INFO_TO, PAYMENT_ID));

    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_status_acsc() {
        // Given
        PaymentBO payment = readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml");
        when(paymentService.getPaymentById(any())).thenReturn(payment);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePaymentCancellation(SCA_INFO_TO, PAYMENT_ID));

    }

    @Test
    void getPendingPeriodicPayments() {
        // Given
        when(scaUtils.userBO(anyString())).thenReturn(new UserBO());
        when(paymentConverter.toPaymentTOList(any())).thenReturn(Collections.singletonList(new PaymentTO()));
        when(paymentService.getPaymentsByTypeStatusAndDebtor(eq(PaymentTypeBO.PERIODIC), eq(ACSP), anySet())).thenReturn(Collections.singletonList(new PaymentBO()));

        // When
        List<PaymentTO> result = middlewareService.getPendingPeriodicPayments(SCA_INFO_TO);

        // Then
        assertTrue(result.size() > 0);
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ScaInfoTO buildScaInfoTO() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORISATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleTO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        BearerTokenTO token = new BearerTokenTO();
        token.setScopes(Collections.singleton("full_access"));
        info.setBearerToken(token);
        return info;
    }

    private DepositAccountBO getAccount(Currency currency, boolean isBlocked) {
        return DepositAccountBO.builder()
                       .iban(IBAN)
                       .currency(currency)
                       .blocked(isBlocked)
                       .build();
    }

    @Test
    void getPendingPeriodicPaymentsPaged() {
        when(scaUtils.userBO(any())).thenReturn(new UserBO());
        when(paymentService.getPaymentsByTypeStatusAndDebtorPaged(any(), any(), anySet(), any())).thenReturn(new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 10));
        middlewareService.getPendingPeriodicPaymentsPaged(buildScaInfoTO(), new CustomPageableImpl(0, 10));
        verify(paymentService, times(1)).getPaymentsByTypeStatusAndDebtorPaged(eq(PaymentTypeBO.PERIODIC), eq(ACSP), anySet(), any());
    }
}

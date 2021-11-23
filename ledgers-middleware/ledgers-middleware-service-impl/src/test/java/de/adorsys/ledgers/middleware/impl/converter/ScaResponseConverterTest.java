package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.impl.sca.EmailScaChallengeData;
import de.adorsys.ledgers.middleware.impl.service.ScaChallengeDataResolverImpl;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScaResponseConverterTest {

    private static final String PSU_MESSAGE = "Message from core-banking to PSU";
    private static final String OPERATION_ID = "Operation ID";
    private static final String AUTHORIZATION_ID = "Authorization ID";
    private static final String AUTH_CODE = "123456";
    private static final String ADDITIONAL_INFORMATION = "Additional info";
    private static final String SCA_ID = "SCA ID";
    private static final String CHALLENGE_DATA_TYPE = "SMTP_OTP";
    private static final LocalDateTime STATUS_DATE = LocalDateTime.of(2020, 1, 1, 12, 12, 12);
    private static final int EXPIRES_IN_SECONDS = 600;
    private static final String USER_EMAIL = "test@mail.de";
    private static final String ACCESS_TOKEN = "eyjhrbhvgdehgtrhgdeherghlhj";

    @InjectMocks
    private ScaResponseConverter converter;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BearerTokenMapper bearerTokenMapper;

    @Mock
    private SCAOperationService scaOperationService;

    @Mock
    private ScaChallengeDataResolverImpl scaChallengeDataResolver;

    private final ScaUserDataTO scaUserDataTO = getScaUserDataTO();

    @BeforeEach
    void init() {
        when(userMapper.toScaUserDataListTO(Collections.singletonList(getScaUserDataBO())))
                .thenReturn(Collections.singletonList(scaUserDataTO));
        when(bearerTokenMapper.toBearerTokenTO(getBearerTokenBO()))
                .thenReturn(getBearerTokenTO());
    }

    @Test
    void mapResponse_consent_notMultilevel() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), false);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.RECEIVED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);

        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertFalse(response.isMultilevelScaRequired());
    }

    @Test
    void mapResponse_consent_multilevel() throws NoSuchFieldException {
        // Given
        when(scaOperationService.authenticationCompleted(any(), any()))
                .thenReturn(true);
        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), true);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.RECEIVED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);

        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertTrue(response.isMultilevelScaRequired());
        assertFalse(response.isPartiallyAuthorised());
    }


    @Test
    void mapResponse_consent_notMultilevel_scaMethodSelected() throws NoSuchFieldException {
        // Given
        when(userMapper.toScaUserDataTO(any()))
                .thenReturn(scaUserDataTO);

        scaChallengeDataResolver.afterPropertiesSet();

        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), false);

        EmailScaChallengeData data = new EmailScaChallengeData();
        Map<String, ChallengeDataTO> datasMap = new HashMap<>();

        ChallengeDataTO challengeDataTO = getChallengeDataTO();

        datasMap.put(CHALLENGE_DATA_TYPE, challengeDataTO);
        FieldSetter.setField(data, data.getClass().getSuperclass().getDeclaredField("challengeDatas"), datasMap);

        when(scaChallengeDataResolver.resolveScaChallengeData(ScaMethodTypeTO.SMTP_OTP))
                .thenReturn(data);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.SCAMETHODSELECTED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);
        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertNotNull(response.getChallengeData());
        assertEquals(challengeDataTO, response.getChallengeData());
    }

    @Test
    void mapResponse_payment_notMultilevel() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), false);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.RECEIVED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);

        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertFalse(response.isMultilevelScaRequired());
    }

    @Test
    void mapResponse_payment_multilevel() throws NoSuchFieldException {
        // Given
        when(scaOperationService.authenticationCompleted(any(), any()))
                .thenReturn(true);
        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), true);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.RECEIVED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);

        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertTrue(response.isMultilevelScaRequired());
        assertFalse(response.isPartiallyAuthorised());
    }

    @Test
    void mapResponse_payment_notMultilevel_scaMethodSelected() throws NoSuchFieldException {
        // Given
        when(userMapper.toScaUserDataTO(any()))
                .thenReturn(scaUserDataTO);

        scaChallengeDataResolver.afterPropertiesSet();

        FieldSetter.setField(converter, converter.getClass().getDeclaredField("multilevelScaEnable"), false);

        EmailScaChallengeData data = new EmailScaChallengeData();
        Map<String, ChallengeDataTO> datasMap = new HashMap<>();

        ChallengeDataTO challengeDataTO = getChallengeDataTO();

        datasMap.put(CHALLENGE_DATA_TYPE, challengeDataTO);
        FieldSetter.setField(data, data.getClass().getSuperclass().getDeclaredField("challengeDatas"), datasMap);

        when(scaChallengeDataResolver.resolveScaChallengeData(ScaMethodTypeTO.SMTP_OTP))
                .thenReturn(data);

        // When
        GlobalScaResponseTO response = converter.mapResponse(getOperation(ScaStatusBO.SCAMETHODSELECTED),
                                                             Collections.singletonList(getScaUserDataBO()),
                                                             PSU_MESSAGE,
                                                             getBearerTokenBO(),
                                                             10, null);
        // Then
        basicFieldsAssert(scaUserDataTO, response);
        assertNotNull(response.getChallengeData());
        assertEquals(challengeDataTO, response.getChallengeData());
    }

    private void basicFieldsAssert(ScaUserDataTO scaUserDataTO, GlobalScaResponseTO response) {
        assertEquals(Collections.singletonList(scaUserDataTO), response.getScaMethods());
        assertEquals(OPERATION_ID, response.getAuthorisationId());
        assertEquals(PSU_MESSAGE, response.getPsuMessage());
        assertEquals(getBearerTokenTO(), response.getBearerToken());
        assertEquals(STATUS_DATE, response.getStatusDate());
        assertEquals(EXPIRES_IN_SECONDS, response.getExpiresInSeconds());
        assertEquals(AUTH_CODE, response.getTan());
    }

    private ChallengeDataTO getChallengeDataTO() {
        ChallengeDataTO challengeDataTO = new ChallengeDataTO();
        challengeDataTO.setAdditionalInformation(ADDITIONAL_INFORMATION);
        challengeDataTO.setData(Collections.singletonList("Data"));
        return challengeDataTO;
    }

    private SCAOperationBO getOperation(ScaStatusBO status) {
        SCAOperationBO operation = new SCAOperationBO();
        operation.setId(OPERATION_ID);
        operation.setScaMethodId("3");
        operation.setScaStatus(status);
        operation.setStatusTime(STATUS_DATE);
        operation.setValiditySeconds(EXPIRES_IN_SECONDS);
        operation.setTan(AUTH_CODE);
        return operation;
    }

    private ScaUserDataBO getScaUserDataBO() {
        return new ScaUserDataBO("3", ScaMethodTypeBO.SMTP_OTP, USER_EMAIL, false, AUTH_CODE, true);
    }

    private ScaUserDataTO getScaUserDataTO() {
        return new ScaUserDataTO("3", ScaMethodTypeTO.SMTP_OTP, USER_EMAIL, new UserTO(), false, AUTH_CODE, false, true);
    }

    private BearerTokenBO getBearerTokenBO() {
        BearerTokenBO token = new BearerTokenBO();
        token.setAccess_token(ACCESS_TOKEN);
        token.setAccessTokenObject(getAccessTokenBO());
        token.setExpires_in(6200);
        token.setRefresh_token("refresh_token");
        token.setToken_type("Bearer");
        return token;
    }

    private BearerTokenTO getBearerTokenTO() {
        BearerTokenTO token = new BearerTokenTO();
        token.setAccess_token(ACCESS_TOKEN);
        token.setAccessTokenObject(getAccessTokenTO());
        token.setExpires_in(6200);
        token.setRefresh_token("refresh_token");
        token.setToken_type("Bearer");
        return token;
    }

    private AccessTokenBO getAccessTokenBO() {
        AccessTokenBO token = new AccessTokenBO();
        token.setScaId(SCA_ID);
        token.setAuthorisationId(AUTHORIZATION_ID);
        return token;
    }

    private AccessTokenTO getAccessTokenTO() {
        AccessTokenTO token = new AccessTokenTO();
        token.setScaId(SCA_ID);
        token.setAuthorisationId(AUTHORIZATION_ID);
        return token;
    }

}
package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.sca.db.domain.AuthCodeStatus.DONE;
import static de.adorsys.ledgers.sca.db.domain.AuthCodeStatus.EXPIRED;
import static de.adorsys.ledgers.sca.db.domain.ScaStatus.FAILED;
import static de.adorsys.ledgers.sca.db.domain.ScaStatus.FINALISED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SCAOperationServiceImplTest {

    private static final String SCA_USER_DATA_ID = "sca_user_data_id";
    private static final String PAYMENT_ID = "payment_id";
    private static final String OP_ID = "opId";
    private static final int VALIDITY_SECONDS = 3 * 60;
    private static final String TAN = "my tan";
    private static final String STATIC_TAN = "my static tan";
    private static final String AUTH_CODE_HASH = "authCodeHash";
    private static final String USER_LOGIN = "user_login";
    private static final String AUTH_ID = "authorisationId";

    @InjectMocks
    private SCAOperationServiceImpl scaOperationService;

    @Mock
    private AuthCodeGenerator authCodeGenerator;

    @Mock
    private SCAOperationRepository repository;

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private SCAOperationMapper scaOperationMapper;

    @Mock
    private Environment env;

    private SCAOperationEntity scaOperationEntity;
    private SCAOperationBO scaOperationBO;
    private SCASender emailSender;
    private AuthCodeDataBO codeDataBO;
    private ScaAuthConfirmationBO scaAuthConfirmationBO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        scaOperationService.setHashGenerator(hashGenerator);
        scaOperationService.setAuthCodeFailedMax(3);
        scaOperationService.setLoginFailedMax(3);

        scaOperationEntity = readFromFile("scaOperationEntity.yml", SCAOperationEntity.class);
        scaOperationEntity.setId(AUTH_ID);
        scaOperationEntity.setCreated(now);
        scaOperationEntity.setStatusTime(now);
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);
        scaOperationEntity.setScaWeight(100);

        scaOperationBO = readFromFile("scaOperationEntity.yml", SCAOperationBO.class);
        scaOperationBO.setId(AUTH_ID);
        scaOperationBO.setCreated(now);
        scaOperationBO.setStatusTime(now);
        scaOperationBO.setScaMethodId(SCA_USER_DATA_ID);

        codeDataBO = new AuthCodeDataBO();
        codeDataBO.setAuthorisationId(AUTH_ID);
        codeDataBO.setOpId(PAYMENT_ID);
        codeDataBO.setScaUserDataId(SCA_USER_DATA_ID);
        codeDataBO.setUserLogin(USER_LOGIN);
        codeDataBO.setOpType(OpTypeBO.PAYMENT);
        codeDataBO.setScaWeight(100);

        HashMap<ScaMethodTypeBO, SCASender> senders = new HashMap<>();
        emailSender = mock(SCASender.class);
        SCASender mobileSender = mock(SCASender.class);
        senders.put(ScaMethodTypeBO.EMAIL, emailSender);
        senders.put(ScaMethodTypeBO.MOBILE, mobileSender);

        scaOperationService.setSenders(senders);
        scaOperationService.setAuthCodeValiditySeconds(VALIDITY_SECONDS);
        scaOperationService.setAuthCodeEmailBody("TAN: %s");

        scaAuthConfirmationBO = new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OP_ID);
    }


    @Test
    void generateAuthCode() throws HashGenerationException {
        // Given
        String email = "spe@adorsys.com.ua";

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);

        // When
        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

        // Then
        assertThat(scaOperationBO.getId(), is(entity.getId()));

        assertThat(entity.getValiditySeconds(), is(VALIDITY_SECONDS));
        assertThat(entity.getCreated(), is(notNullValue()));
        assertThat(entity.getStatus(), is(AuthCodeStatus.SENT));
        assertThat(entity.getStatusTime(), is(notNullValue()));
        assertThat(entity.getHashAlg(), is(HashGeneratorImpl.DEFAULT_HASH_ALG));
        assertThat(entity.getAuthCodeHash(), is(notNullValue()));

        verify(authCodeGenerator, times(1)).generate();
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void generateAuthCode_email_valid() throws HashGenerationException {
        // Given
        String email = "spe@adorsys.com.ua";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);

        // When
        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

        // Then
        assertThat(scaOperationBO.getId(), is(entity.getId()));

        assertThat(entity.getValiditySeconds(), is(VALIDITY_SECONDS));
        assertThat(entity.getCreated(), is(notNullValue()));
        assertThat(entity.getStatus(), is(AuthCodeStatus.SENT));
        assertThat(entity.getStatusTime(), is(notNullValue()));
        assertThat(entity.getHashAlg(), is(HashGeneratorImpl.DEFAULT_HASH_ALG));
        assertThat(entity.getAuthCodeHash(), is(notNullValue()));

        verify(authCodeGenerator, times(1)).generate();
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(entity);
        verify(emailSender, times(1)).send(anyString(), anyString());
    }

    @Test
    void generateAuthCode_tan_withoutStaticTan() throws HashGenerationException {
        //usesStaticTan = false / staticTan = null
        when(authCodeGenerator.generate()).thenReturn(TAN);

        String email = "spe@adorsys.com.ua";
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        method.setUsesStaticTan(false);
        method.setStaticTan(TAN);
        userBO.setScaUserData(Collections.singletonList(method));

        prepareMockedData(userBO, method);

        codeDataBO.setAuthorisationId(AUTH_ID);

        // When
        SCAOperationBO envIsSandbox = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        // Then
        assertThat(envIsSandbox.getTan(), is(TAN));
    }

    @Test
    void generateAuthCode_tan_withNullStaticTan() throws HashGenerationException {
        // Given
        //usesStaticTan = true / staticTan = null
        String email = "spe@adorsys.com.ua";
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        method.setUsesStaticTan(true);
        method.setStaticTan(TAN);
        userBO.setScaUserData(Collections.singletonList(method));

        prepareMockedData(userBO, method);

        codeDataBO.setAuthorisationId(AUTH_ID);

        // When
        SCAOperationBO usesStaticTan = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        // Then
        assertThat(usesStaticTan.getTan(), is(TAN));
    }

    @Test
    void generateAuthCode_data_sca_method_id_null() throws HashGenerationException {
        // Given
        scaOperationEntity.setScaMethodId(null);
        // Then
        assertThrows(ScaModuleException.class, () -> callGenerateAuthCode(false, false, false, true, true));
    }

    @Test
    void generateAuthCode_auth_code_data_id_is_null() throws HashGenerationException {
        // Given
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);
        // Then
        assertThrows(ScaModuleException.class, () -> callGenerateAuthCode(false, false, true, false, true));
    }

    @Test
    void generateAuthCode_code_data_is_null_or_empty() throws HashGenerationException {
        // Given
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);
        // Then
        assertThrows(ScaModuleException.class, this::callGenerateAuthCodeCode_scaUserDataIsNull);
    }

    @Test
    void generateAuthCode_sca_method_not_supported() throws HashGenerationException {
        // Given
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);

        // Then
        assertThrows(ScaModuleException.class, () -> callGenerateAuthCode(true, true, true, true, false));
    }

    @Test
    void generateAuthCode_email_valid_user_msg_not_empty() throws HashGenerationException {
        // Given
        String email = "spe@adorsys.com.ua";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(emailSender.send(email, "TAN: my tan")).thenReturn(true);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);

        codeDataBO.setUserMessage("my tan");
        // When
        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

        // Then
        assertThat(scaOperationBO.getId(), is(entity.getId()));

        assertThat(entity.getValiditySeconds(), is(VALIDITY_SECONDS));
        assertThat(entity.getCreated(), is(notNullValue()));
        assertThat(entity.getStatus(), is(AuthCodeStatus.SENT));
        assertThat(entity.getStatusTime(), is(notNullValue()));
        assertThat(entity.getHashAlg(), is(HashGeneratorImpl.DEFAULT_HASH_ALG));
        assertThat(entity.getAuthCodeHash(), is(notNullValue()));

        verify(authCodeGenerator, times(1)).generate();
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(entity);
        verify(emailSender, times(1)).send(anyString(), anyString());
    }

    @Test
    void generateAuthCodeWithException() throws HashGenerationException {
        // Given
        String email = "spe@adorsys.com.ua";
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        UserBO userBO = mock(UserBO.class);

        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED));
    }

    @Test
    void scaDataNotFound() {
        // Given
        String email = "spe@adorsys.com.ua";
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId("not existing id");
        UserBO userBO = mock(UserBO.class);

        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED));
    }

    @Test
    void validateAuthCode() throws HashGenerationException {
        // Given
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));

        // When
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0);

        // Then
        assertThat(scaValidationBO.isValidAuthCode(), is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.VALIDATED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    void validateAuthCode_authConfirmationEnabled() throws HashGenerationException, NoSuchFieldException {
        // Given
        FieldSetter.setField(scaOperationService, scaOperationService.getClass().getDeclaredField("authConfirmationEnabled"), true);
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));

        // When
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0);

        // Then
        assertThat(scaValidationBO.isValidAuthCode(), is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.VALIDATED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));
        assertThat(StringUtils.isNotEmpty(scaValidationBO.getAuthConfirmationCode()), is(true));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(2)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    void validateAuthCode_same_operation() throws HashGenerationException {
        // Given
        scaOperationEntity.setOpId("wrong id");

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCodeNotValid() throws HashGenerationException {
        // Given
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn("wrong hash");

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));

        verify(repository, times(2)).findById(AUTH_ID);
        verify(hashGenerator, times(1)).hash(any());
    }

    @Test
    void validateAuthCodeOperationNotFound() {
        when(repository.findById(AUTH_ID)).thenReturn(Optional.empty());
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCodeOperationHashException() throws HashGenerationException {
        // Given
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCodeOperationStolenException() {
        // operation was used
        scaOperationEntity.setStatus(AuthCodeStatus.SENT);

        // Force validated status to trigger expected exception
        scaOperationEntity.setStatus(AuthCodeStatus.VALIDATED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCodeOperationExpiredException() {

        // operation was created 5 minutes ago
        scaOperationEntity.setCreated(LocalDateTime.now().minusMinutes(5));

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(repository.save(any())).thenReturn(scaOperationEntity);

        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCode_operation_is_used_expired() {
        scaOperationEntity.setStatus(EXPIRED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));

        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCode_operation_is_used_done() {
        scaOperationEntity.setStatus(DONE);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));

    }

    @Test
    void validateAuthCode_operation_is_used_sca_failed() {
        scaOperationEntity.setScaStatus(FAILED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @Test
    void validateAuthCode_operation_is_used_sca_finalized() {
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        assertThrows(ScaModuleException.class, () -> scaOperationService.validateAuthCode(AUTH_ID, OP_ID, TAN, 0));
    }

    @SuppressWarnings("unchecked")
    @Test
    void processExpiredOperations() throws IOException {
        // Given
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream("list-of-sca-operations.yml");
        List<SCAOperationEntity> list = objectMapper.readValue(inputStream, new TypeReference<List<SCAOperationEntity>>() {
        });
        list.add(scaOperationEntity);

        assertThat(list.size(), is(3));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        when(repository.findByStatus(AuthCodeStatus.SENT)).thenReturn(list);
        when(repository.saveAll(captor.capture())).thenReturn(mock(List.class));

        // When
        scaOperationService.processExpiredOperations();

        List<SCAOperationEntity> expiredOperations = captor.getValue();

        // Then
        assertThat(expiredOperations.size(), is(2));

        assertThat(expiredOperations.get(0).getOpId(), is("opId1"));
        assertThat(expiredOperations.get(0).getStatus(), is(EXPIRED));
        // status was updated less then one minute ago
        assertThat(expiredOperations.get(0).getStatusTime().isAfter(LocalDateTime.now().minusMinutes(1)), is(true));

        assertThat(expiredOperations.get(1).getOpId(), is("opId2"));
        assertThat(expiredOperations.get(1).getStatus(), is(EXPIRED));
        // status was updated less then one minute ago
        assertThat(expiredOperations.get(1).getStatusTime().isAfter(LocalDateTime.now().minusMinutes(1)), is(true));

        verify(repository, times(1)).findByStatus(AuthCodeStatus.SENT);
        verify(repository, times(1)).saveAll(expiredOperations);
    }

    @Test
    void createAuthCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        when(scaOperationMapper.toBO(any(SCAOperationEntity.class))).thenReturn(getScaOperationBO(now));
        when(repository.save(any())).thenReturn(scaOperationEntity);

        // When
        SCAOperationBO result = scaOperationService.createAuthCode(codeDataBO, ScaStatusBO.FINALISED);

        // Then
        assertThat(result, is(getScaOperationBO(now)));
    }

    @Test
    void createAuthCode_null_authorization_id() {
        // Given
        codeDataBO.setAuthorisationId(null);

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.createAuthCode(codeDataBO, ScaStatusBO.FINALISED));
    }

    @Test
    void loadAuthCode() {
        // Given
        when(repository.findById(anyString())).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(any(SCAOperationEntity.class))).thenReturn(scaOperationBO);

        // When
        SCAOperationBO result = scaOperationService.loadAuthCode(AUTH_ID);

        // Then
        assertThat(result, is(scaOperationBO));
    }

    @Test
    void loadAuthCode_not_found() {
        // Given
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.loadAuthCode(AUTH_ID));
    }

    @Test
    void authenticationCompleted() {
        // Given
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));

        // When
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);

        // Then
        assertTrue(result);
    }

    @Test
    void authenticationCompleted_non_sca_finalized() {
        // Given
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));

        // When
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);

        // Then
        assertFalse(result);
    }

    @Test
    void authenticationCompleted_multilevel() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(scaOperationService, scaOperationService.getClass().getDeclaredField("multilevelScaEnable"), true);
        FieldSetter.setField(scaOperationService, scaOperationService.getClass().getDeclaredField("finalWeight"), 100);
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));

        // When
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);

        // Then
        assertTrue(result);
    }

    @Test
    void authenticationCompleted_multilevel_unfinished_sca() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(scaOperationService, scaOperationService.getClass().getDeclaredField("multilevelScaEnable"), true);
        FieldSetter.setField(scaOperationService, scaOperationService.getClass().getDeclaredField("finalWeight"), 100);
        scaOperationEntity.setScaWeight(80);
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));

        // When
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);

        // Then
        assertFalse(result);
    }

    @Test
    void verifyAuthConfirmationCode() {
        // Given
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);

        // When
        ScaAuthConfirmationBO result = scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN);

        // Then
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test
    void verifyAuthConfirmationCode_op_not_found() {
        // Given
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.empty());

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN));
    }

    @Test
    void verifyAuthConfirmationCode_wrong_hash() {
        // Given
        scaOperationEntity.setOpType(OpType.PAYMENT);
        scaAuthConfirmationBO.setConfirm(false);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH + 1);

        // When
        ScaAuthConfirmationBO result = scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN);

        // Then
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test
    void completeAuthConfirmation() {
        // Given
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));

        // When
        ScaAuthConfirmationBO result = scaOperationService.completeAuthConfirmation(AUTH_ID, true);

        // Then
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test
    void checkIfExistsOrNew() {
        when(repository.findById(anyString())).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(any(SCAOperationEntity.class))).thenReturn(scaOperationBO);
        SCAOperationBO result = scaOperationService.checkIfExistsOrNew(codeDataBO);
        assertThat(result, is(scaOperationBO));
    }

    @Test
    void updateFailedCount() {
        // Given
        when(repository.findById(anyString())).thenReturn(Optional.of(scaOperationEntity));

        // When
        ScaModuleException result = scaOperationService.updateFailedCount(AUTH_ID, true);

        // Then
        assertThat(result.getDevMsg().contains("2"), is(true));
    }

    @Test
    void updateFailedCount_fail() {
        // Given
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(ScaModuleException.class, () -> scaOperationService.updateFailedCount(AUTH_ID, false));
    }

    private void callGenerateAuthCode(boolean usesStaticTan, boolean staticTanPresent, boolean methodIdPresent, boolean methodIsPresent, boolean methodSupported) {
        String email = "spe@adorsys.com.ua";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(methodSupported ? ScaMethodTypeBO.EMAIL : ScaMethodTypeBO.APP_OTP, email);
        method.setId(SCA_USER_DATA_ID);
        codeDataBO.setScaUserDataId(methodIdPresent ? SCA_USER_DATA_ID : null);
        method.setValid(true);
        method.setUsesStaticTan(usesStaticTan);
        method.setStaticTan(staticTanPresent ? STATIC_TAN : TAN);
        userBO.setScaUserData(methodIsPresent ? Collections.singletonList(method) : Collections.emptyList());

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));

        codeDataBO.setAuthorisationId(AUTH_ID);

        scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
    }

    private void prepareMockedData(UserBO userBO, ScaUserDataBO method) {
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(env.getActiveProfiles()).thenReturn(new String[]{"sandbox"});

        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);
    }

    private void callGenerateAuthCodeCode_scaUserDataIsNull() {
        String email = "spe@adorsys.com.ua";
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        codeDataBO.setScaUserDataId(null);
        method.setValid(true);
        method.setUsesStaticTan(false);
        method.setStaticTan(TAN);
        userBO.setScaUserData(Collections.singletonList(method));

        codeDataBO.setAuthorisationId(null);

        scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
    }

    private SCAOperationBO getScaOperationBO(LocalDateTime statusTime) {
        SCAOperationBO bo = new SCAOperationBO();
        bo.setId(AUTH_ID);
        bo.setOpId(PAYMENT_ID);
        bo.setValiditySeconds(VALIDITY_SECONDS);
        bo.setStatus(AuthCodeStatusBO.INITIATED);
        bo.setStatusTime(statusTime);
        bo.setOpType(OpTypeBO.PAYMENT);
        bo.setScaMethodId(SCA_USER_DATA_ID);
        bo.setScaStatus(ScaStatusBO.FINALISED);
        return bo;
    }

    private <T> T readFromFile(String file, Class<T> tClass) {
        InputStream is = SCAOperationServiceImplTest.class.getResourceAsStream(file);
        try {
            return YamlReader.getInstance().getObjectFromInputStream(is, tClass);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

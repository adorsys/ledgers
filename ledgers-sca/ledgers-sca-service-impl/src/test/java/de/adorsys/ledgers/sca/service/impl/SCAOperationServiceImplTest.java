package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.domain.ScaStatus;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SCAOperationServiceImplTest {

    private static final String SCA_USER_DATA_ID = "sca_user_data_id";
    private static final String PAYMENT_ID = "payment_id";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
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
    private UserService userService;

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private SCAOperationMapper scaOperationMapper;

    @Mock
    private Environment env;

    private SCAOperationEntity scaOperationEntity;
    private SCAOperationBO scaOperationBO;
    private SCASender emailSender;
    private SCASender mobileSender;
    private AuthCodeDataBO codeDataBO;
    private ScaAuthConfirmationBO scaAuthConfirmationBO;

    @Before
    public void setUp() {
        LocalDateTime now = LocalDateTime.now();
        scaOperationService.setHashGenerator(hashGenerator);

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
        codeDataBO.setOpData(OP_DATA);
        codeDataBO.setOpId(PAYMENT_ID);
        codeDataBO.setScaUserDataId(SCA_USER_DATA_ID);
        codeDataBO.setUserLogin(USER_LOGIN);
        codeDataBO.setOpType(OpTypeBO.PAYMENT);
        codeDataBO.setScaWeight(100);

        HashMap<ScaMethodTypeBO, SCASender> senders = new HashMap<>();
        emailSender = mock(SCASender.class);
        mobileSender = mock(SCASender.class);
        senders.put(ScaMethodTypeBO.EMAIL, emailSender);
        senders.put(ScaMethodTypeBO.MOBILE, mobileSender);

        scaOperationService.setSenders(senders);
        scaOperationService.setAuthCodeValiditySeconds(VALIDITY_SECONDS);
        scaOperationService.setAuthCodeEmailBody("TAN: %s");

        scaAuthConfirmationBO = new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OP_ID);
    }


    private <T> T readFromFile(String file, Class<T> tClass) {
        InputStream is = SCAOperationServiceImplTest.class.getResourceAsStream(file);
        try {
            return YamlReader.getInstance().getObjectFromInputStream(is, tClass);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Test
    public void generateAuthCode() throws HashGenerationException {
        String email = "spe@adorsys.com.ua";

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(emailSender.send(email, TAN)).thenReturn(true);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);


        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

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
    public void generateAuthCode_email_valid() throws HashGenerationException {
        String email = "spe@adorsys.com.ua";

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(emailSender.send(email, TAN)).thenReturn(true);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);


        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

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
    public void generateAuthCode_tan_strategies() throws HashGenerationException {
        //usesStaticTan = false / staticTan = null
        SCAOperationBO envIsSandbox = callGenerateAuthCode(true, false, false, true, true, true);
        assertThat(envIsSandbox.getTan(), is(TAN));

        //usesStaticTan = false / staticTan = null
        SCAOperationBO usesStaticTan = callGenerateAuthCode(true, true, false, true, true, true);
        assertThat(usesStaticTan.getTan(), is(TAN));

        //usesStaticTan = true / staticTan = "my tan"
        SCAOperationBO staticTanSet = callGenerateAuthCode(true, true, true, true, true, true);
        assertThat(staticTanSet.getTan(), is(STATIC_TAN));

    }

    @Test
    public void generateAuthCode_sca_method_id_null() throws HashGenerationException {
        scaOperationEntity.setScaMethodId(null);
        SCAOperationBO result = callGenerateAuthCode(true, false, false, true, true, true);
        assertThat(result.getScaMethodId(), is(SCA_USER_DATA_ID));
    }

    @Test(expected = ScaModuleException.class)
    public void generateAuthCode_data_sca_method_id_null() throws HashGenerationException {
        scaOperationEntity.setScaMethodId(null);
        callGenerateAuthCode(true, false, false, false, true, true);
    }

    @Test(expected = ScaModuleException.class)
    public void generateAuthCode_auth_code_data_id_is_null() throws HashGenerationException {
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);
        callGenerateAuthCode(true, false, false, true, false, true);
    }

    @Test(expected = ScaModuleException.class)
    public void generateAuthCode_code_data_is_null_or_empty() throws HashGenerationException {
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);
        callGenerateAuthCode(false, false, false, false, true, true);
    }

    @Test(expected = ScaModuleException.class)
    public void generateAuthCode_sca_method_not_supported() throws HashGenerationException {
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);

        SCAOperationBO result = callGenerateAuthCode(true, true, true, true, true, false);
        assertThat(result.getScaMethodId(), is(SCA_USER_DATA_ID));
    }

    private SCAOperationBO callGenerateAuthCode(boolean codeDataAuthIdPresent, boolean usesStaticTan, boolean staticTanPresent, boolean methodIdPresent, boolean methodIsPresent, boolean methodSupported) {
        String email = "spe@adorsys.com.ua";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);
        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(methodSupported ? ScaMethodTypeBO.EMAIL : ScaMethodTypeBO.APP_OTP, email);
        method.setId(SCA_USER_DATA_ID );
        codeDataBO.setScaUserDataId(methodIdPresent ? SCA_USER_DATA_ID : null);
        method.setValid(true);
        method.setUsesStaticTan(usesStaticTan);
        method.setStaticTan(staticTanPresent ? STATIC_TAN : TAN);
        userBO.setScaUserData(methodIsPresent ? Collections.singletonList(method) : Collections.emptyList());

        when(env.getActiveProfiles()).thenReturn(new String[]{"sandbox"});
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(methodIsPresent ? Collections.singletonList(method) : Collections.emptyList());
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(emailSender.send(email, TAN)).thenReturn(true);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);

        codeDataBO.setAuthorisationId(codeDataAuthIdPresent ? AUTH_ID : null);

        return scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
    }

    @Test
    public void generateAuthCode_email_valid_user_msg_not_empty() throws HashGenerationException {
        String email = "spe@adorsys.com.ua";

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        UserBO userBO = mock(UserBO.class);
        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        method.setValid(true);
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(emailSender.send(email, TAN)).thenReturn(true);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(scaOperationEntity)).thenReturn(scaOperationBO);

        codeDataBO.setUserMessage("user msg");
        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);

        SCAOperationEntity entity = captor.getValue();

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

    @Test(expected = ScaModuleException.class)
    public void generateAuthCodeWithException() throws HashGenerationException {
        String email = "spe@adorsys.com.ua";

        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId(SCA_USER_DATA_ID);
        UserBO userBO = mock(UserBO.class);

        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});

        scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
    }

    @Test(expected = ScaModuleException.class)
    public void scaDataNotFound() {
        String email = "spe@adorsys.com.ua";

        ScaUserDataBO method = new ScaUserDataBO(ScaMethodTypeBO.EMAIL, email);
        method.setId("not existing id");
        UserBO userBO = mock(UserBO.class);

        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);
        when(userBO.getScaUserData()).thenReturn(Collections.singletonList(method));
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));

        scaOperationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
    }

    @Test
    public void validateAuthCode() throws HashGenerationException {

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        assertThat(scaValidationBO.isValidAuthCode(), is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.VALIDATED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    public void validateAuthCode_authConfirmationEnabled() throws HashGenerationException {
        Whitebox.setInternalState(scaOperationService, "authConfirmationEnabled", true);
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        assertThat(scaValidationBO.isValidAuthCode(), is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.VALIDATED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));
        assertThat(StringUtils.isNotEmpty(scaValidationBO.getAuthConfirmationCode()), is(true));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(2)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCode_same_operation() throws HashGenerationException {
        scaOperationEntity.setOpId("wrong id");
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        verify(repository, times(1)).findById(AUTH_ID);
    }

    @Test
    public void validateAuthCodeNotValid() throws HashGenerationException {

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn("wrong hash");
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        assertThat(scaValidationBO.isValidAuthCode(), is(Boolean.FALSE));

        assertThat(scaOperationEntity.getStatus(), is(AuthCodeStatus.FAILED));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(1)).hash(any());
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCodeOperationNotFound() {
        when(repository.findById(AUTH_ID)).thenReturn(Optional.empty());

        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCodeOperationHashException() throws HashGenerationException {
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCodeOperationStolenException() {

        // operation was used
        scaOperationEntity.setStatus(AuthCodeStatus.SENT);

        // Force validated status to trigger expected exception
        scaOperationEntity.setStatus(AuthCodeStatus.VALIDATED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));

        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCodeOperationExpiredException() {

        // operation was created 5 minutes ago
        scaOperationEntity.setCreated(LocalDateTime.now().minusMinutes(5));

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(repository.save(any())).thenReturn(scaOperationEntity);

        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCode_operation_is_used_expired() {
        scaOperationEntity.setStatus(EXPIRED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCode_operation_is_used_done() {
        scaOperationEntity.setStatus(DONE);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCode_operation_is_used_sca_failed() {
        scaOperationEntity.setScaStatus(FAILED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @Test(expected = ScaModuleException.class)
    public void validateAuthCode_operation_is_used_sca_finalized() {
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processExpiredOperations() throws IOException {

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

        scaOperationService.processExpiredOperations();

        List<SCAOperationEntity> expiredOperations = captor.getValue();

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
    public void createAuthCode() {
        LocalDateTime now = LocalDateTime.now();
        when(scaOperationMapper.toBO(any(SCAOperationEntity.class))).thenAnswer(a -> getScaOperationBO(now));
        SCAOperationBO result = scaOperationService.createAuthCode(codeDataBO, ScaStatusBO.FINALISED);
        assertThat(result, is(getScaOperationBO(now)));
    }

    @Test(expected = ScaModuleException.class)
    public void createAuthCode_null_authorization_id() {
        LocalDateTime now = LocalDateTime.now();
        codeDataBO.setAuthorisationId(null);
        scaOperationService.createAuthCode(codeDataBO, ScaStatusBO.FINALISED);
    }

    @Test
    public void loadAuthCode() {
        when(repository.findById(anyString())).thenReturn(Optional.of(scaOperationEntity));
        when(scaOperationMapper.toBO(any(SCAOperationEntity.class))).thenReturn(scaOperationBO);
        SCAOperationBO result = scaOperationService.loadAuthCode(AUTH_ID);
        assertThat(result, is(scaOperationBO));
    }

    @Test(expected = ScaModuleException.class)
    public void loadAuthCode_not_found() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        SCAOperationBO result = scaOperationService.loadAuthCode(AUTH_ID);
        assertThat(result, is(scaOperationBO));
    }

    @Test
    public void authenticationCompleted() {
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);
        assertThat(result, is(true));
    }

    @Test
    public void authenticationCompleted_non_sca_finalized() {
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);
        assertThat(result, is(false));
    }

    @Test
    public void authenticationCompleted_multilevel() {
        Whitebox.setInternalState(scaOperationService, "multilevelScaEnable", true);
        Whitebox.setInternalState(scaOperationService, "finalWeight", 100);
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);
        assertThat(result, is(true));
    }

    @Test
    public void authenticationCompleted_multilevel_unfinished_sca() {
        Whitebox.setInternalState(scaOperationService, "multilevelScaEnable", true);
        Whitebox.setInternalState(scaOperationService, "finalWeight", 100);
        scaOperationEntity.setScaWeight(80);
        scaOperationEntity.setScaStatus(FINALISED);
        when(repository.findByOpIdAndOpType(anyString(), any())).thenReturn(Collections.singletonList(scaOperationEntity));
        boolean result = scaOperationService.authenticationCompleted(OP_ID, OpTypeBO.PAYMENT);
        assertThat(result, is(false));
    }

    @Test
    public void verifyAuthConfirmationCode() {
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        ScaAuthConfirmationBO result = scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN);
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test(expected = ScaModuleException.class)
    public void verifyAuthConfirmationCode_op_not_found() {
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.empty());
        ScaAuthConfirmationBO result = scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN);
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test
    public void verifyAuthConfirmationCode_wrong_hash() {
        scaOperationEntity.setOpType(OpType.PAYMENT);
        scaAuthConfirmationBO.setConfirm(false);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH + 1);
        ScaAuthConfirmationBO result = scaOperationService.verifyAuthConfirmationCode(AUTH_ID, TAN);
        assertThat(result, is(scaAuthConfirmationBO));
    }

    @Test
    public void completeAuthConfirmation() {
        scaOperationEntity.setOpType(OpType.PAYMENT);
        when(repository.findByIdAndScaStatus(anyString(), any())).thenReturn(Optional.of(scaOperationEntity));
        ScaAuthConfirmationBO result = scaOperationService.completeAuthConfirmation(AUTH_ID, true);
        assertThat(result, is(scaAuthConfirmationBO));
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
}

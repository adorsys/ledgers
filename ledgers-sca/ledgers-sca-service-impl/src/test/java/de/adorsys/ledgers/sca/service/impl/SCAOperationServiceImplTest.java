package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.ScaModuleException;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    private static final String AUTH_CODE_HASH = "authCodeHash";
    private static final String USER_LOGIN = "user_login";
    private static final String USER_MESSAGE = "user message";
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

    @Before
    public void setUp() {
        LocalDateTime now = LocalDateTime.now();
        scaOperationService.setHashGenerator(hashGenerator);

        scaOperationEntity = readFromFile("scaOperationEntity.yml", SCAOperationEntity.class);
        scaOperationEntity.setId(AUTH_ID);
        scaOperationEntity.setCreated(now);
        scaOperationEntity.setStatusTime(now);
        scaOperationEntity.setScaMethodId(SCA_USER_DATA_ID);

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
        boolean valid = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        assertThat(valid, is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.VALIDATED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));

        verify(repository, times(1)).findById(AUTH_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    public void validateAuthCodeNotValid() throws HashGenerationException {

        when(repository.findById(AUTH_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn("wrong hash");
        when(env.getActiveProfiles()).thenReturn(new String[]{"develop"});
        boolean valid = scaOperationService.validateAuthCode(AUTH_ID, OP_ID, OP_DATA, TAN, 0);

        assertThat(valid, is(Boolean.FALSE));

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
        assertThat(expiredOperations.get(0).getStatus(), is(AuthCodeStatus.EXPIRED));
        // status was updated less then one minute ago
        assertThat(expiredOperations.get(0).getStatusTime().isAfter(LocalDateTime.now().minusMinutes(1)), is(true));

        assertThat(expiredOperations.get(1).getOpId(), is("opId2"));
        assertThat(expiredOperations.get(1).getStatus(), is(AuthCodeStatus.EXPIRED));
        // status was updated less then one minute ago
        assertThat(expiredOperations.get(1).getStatusTime().isAfter(LocalDateTime.now().minusMinutes(1)), is(true));

        verify(repository, times(1)).findByStatus(AuthCodeStatus.SENT);
        verify(repository, times(1)).saveAll(expiredOperations);
    }
}

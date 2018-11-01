package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
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
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SCAOperationServiceImplTest {

    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
    private static final int VALIDITY_SECONDS = 3 * 60;
    private static final String TAN = "my tan";
    private static final String AUTH_CODE_HASH = "authCodeHash";

    @InjectMocks
    private SCAOperationServiceImpl scaOperationService;

    @Mock
    private AuthCodeGenerator authCodeGenerator;

    @Mock
    private SCAOperationRepository repository;

    @Mock
    private HashGenerator hashGenerator;
    private SCAOperationEntity scaOperationEntity;

    @Before
    public void setUp() {
        scaOperationService.setHashGenerator(hashGenerator);
        scaOperationEntity = readFromFile("scaOperationEntity.yml", SCAOperationEntity.class);
        scaOperationEntity.setCreated(LocalDateTime.now());
        scaOperationEntity.setStatusTime(LocalDateTime.now());
    }


    private <T> T readFromFile(String file, Class<T> tClass) {
        String fileName = "de/adorsys/ledgers/sca/service/impl/" + file;
        try {
            return YamlReader.getInstance().getObjectFromFile(fileName, tClass);
        } catch (IOException e) {
            throw new IllegalStateException("Resource "+fileName+" not found");
        }
    }

    @Test
    public void generateAuthCode() throws AuthCodeGenerationException, HashGenerationException {

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));


        String tan = scaOperationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);

        assertThat(tan, is(TAN));

        SCAOperationEntity entity = captor.getValue();
        assertThat(entity.getOpId(), is(OP_ID));
        assertThat(entity.getValiditySeconds(), is(VALIDITY_SECONDS));
        assertThat(entity.getCreated(), is(notNullValue()));
        assertThat(entity.getStatus(), is(AuthCodeStatus.NEW));
        assertThat(entity.getStatusTime(), is(notNullValue()));
        assertThat(entity.getHashAlg(), is(HashGeneratorImpl.DEFAULT_HASH_ALG));
        assertThat(entity.getAuthCodeHash(), is(notNullValue()));

        verify(authCodeGenerator, times(1)).generate();
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(entity);
    }

    @Test(expected = AuthCodeGenerationException.class)
    public void generateAuthCodeWithException() throws AuthCodeGenerationException, HashGenerationException {

        when(authCodeGenerator.generate()).thenReturn(TAN);
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        scaOperationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
    }

    @Test
    public void validateAuthCode() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn(AUTH_CODE_HASH);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));

        boolean valid = scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);

        assertThat(valid, is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.USED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));

        verify(repository, times(1)).findById(OP_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    public void validateAuthCodeNotValid() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenReturn("wrong hash");

        boolean valid = scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);

        assertThat(valid, is(Boolean.FALSE));

        assertThat(scaOperationEntity.getStatus(), is(AuthCodeStatus.NEW));

        verify(repository, times(1)).findById(OP_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(0)).save(scaOperationEntity);
    }

    @Test(expected = SCAOperationNotFoundException.class)
    public void validateAuthCodeOperationNotFound() throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        when(repository.findById(OP_ID)).thenReturn(Optional.empty());

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);
    }

    @Test(expected = SCAOperationValidationException.class)
    public void validateAuthCodeOperationHashException() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);
    }

    @Test(expected = SCAOperationUsedOrStolenException.class)
    public void validateAuthCodeOperationStolenException() throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        // operation was used
        scaOperationEntity.setStatus(AuthCodeStatus.USED);

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperationEntity));

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);
    }

    @Test(expected = SCAOperationExpiredException.class)
    public void validateAuthCodeOperationExpiredException() throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        // operation was created 5 minutes ago
        scaOperationEntity.setCreated(LocalDateTime.now().minusMinutes(5));

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperationEntity));
        when(repository.save(any())).thenReturn(scaOperationEntity);

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, TAN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void processExpiredOperations() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream("list-of-sca-operations.yml");
        List<SCAOperationEntity> list = objectMapper.readValue(inputStream, new TypeReference<List<SCAOperationEntity>>() {});
        list.add(scaOperationEntity);

        assertThat(list.size(), is(3));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        when(repository.findByStatus(AuthCodeStatus.NEW)).thenReturn(list);
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

        verify(repository, times(1)).findByStatus(AuthCodeStatus.NEW);
        verify(repository, times(1)).saveAll(expiredOperations);
    }
}
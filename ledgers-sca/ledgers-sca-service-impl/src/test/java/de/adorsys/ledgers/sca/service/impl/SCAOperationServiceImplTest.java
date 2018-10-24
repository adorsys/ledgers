package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.exception.TanGenerationException;
import de.adorsys.ledgers.sca.service.TanGenerator;
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

import javax.swing.plaf.TreeUI;
import java.time.LocalDateTime;
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

    @InjectMocks
    private SCAOperationServiceImpl scaOperationService;

    @Mock
    private TanGenerator tanGenerator;

    @Mock
    private SCAOperationRepository repository;

    @Mock
    private HashGenerator hashGenerator;

    @Before
    public void setUp() {
        scaOperationService.setHashGenerator(hashGenerator);
    }

    @Test
    public void generateAuthCode() throws TanGenerationException, HashGenerationException {

        String myTan = "myTan";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        when(tanGenerator.generate()).thenReturn(myTan);
        when(hashGenerator.hash(any())).thenReturn("hashed object");
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));


        String tan = scaOperationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);

        assertThat(tan, is(myTan));

        SCAOperationEntity entity = captor.getValue();
        assertThat(entity.getOpId(), is(OP_ID));
        assertThat(entity.getValiditySeconds(), is(VALIDITY_SECONDS));
        assertThat(entity.getCreated(), is(notNullValue()));
        assertThat(entity.getStatus(), is(AuthCodeStatus.NEW));
        assertThat(entity.getStatusTime(), is(notNullValue()));
        assertThat(entity.getHashAlg(), is(HashGeneratorImpl.DEFAULT_HASH_ALG));
        assertThat(entity.getAuthCodeHash(), is(notNullValue()));

        verify(tanGenerator, times(1)).generate();
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(entity);
    }
    @Test(expected = TanGenerationException.class)
    public void generateAuthCodeWithException() throws TanGenerationException, HashGenerationException {

        when(tanGenerator.generate()).thenReturn("tan");
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        scaOperationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
    }

    @Test
    public void validateAuthCode() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException {

        String hashedObject = "hashed object";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        SCAOperationEntity entity = new SCAOperationEntity();
        entity.setAuthCodeHash(hashedObject);

        when(repository.findById(OP_ID)).thenReturn(Optional.of(entity));
        when(hashGenerator.hash(any())).thenReturn(hashedObject);
        when(repository.save(captor.capture())).thenReturn(mock(SCAOperationEntity.class));

        boolean valid = scaOperationService.validateAuthCode(OP_ID, OP_DATA, "my tan");

        assertThat(valid, is(Boolean.TRUE));

        SCAOperationEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus(), is(AuthCodeStatus.USED));
        assertThat(savedEntity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.TRUE));

        verify(repository, times(1)).findById(OP_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(1)).save(savedEntity);
    }

    @Test
    public void validateAuthCodeNotValid() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException {

        String hashedObject = "right hash";
        ArgumentCaptor<SCAOperationEntity> captor = ArgumentCaptor.forClass(SCAOperationEntity.class);

        SCAOperationEntity entity = new SCAOperationEntity();
        entity.setStatus(AuthCodeStatus.NEW);
        entity.setStatusTime(LocalDateTime.now().minusSeconds(60));
        entity.setAuthCodeHash(hashedObject);

        when(repository.findById(OP_ID)).thenReturn(Optional.of(entity));
        when(hashGenerator.hash(any())).thenReturn("wrong hash");

        boolean valid = scaOperationService.validateAuthCode(OP_ID, OP_DATA, "my tan");

        assertThat(valid, is(Boolean.FALSE));

        assertThat(entity.getStatus(), is(AuthCodeStatus.NEW));
        assertThat(entity.getStatusTime().isAfter(LocalDateTime.now().minusSeconds(5)), is(Boolean.FALSE));

        verify(repository, times(1)).findById(OP_ID);
        verify(hashGenerator, times(1)).hash(any());
        verify(repository, times(0)).save(entity);
    }

    @Test(expected = SCAOperationNotFoundException.class)
    public void validateAuthCodeOperationNotFound() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException {

        when(repository.findById(OP_ID)).thenReturn(Optional.empty());

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, "my tan");
    }

    @Test(expected = SCAOperationValidationException.class)
    public void validateAuthCodeOperationHashException() throws SCAOperationNotFoundException, SCAOperationValidationException, HashGenerationException {

        SCAOperationEntity scaOperation = mock(SCAOperationEntity.class);

        when(repository.findById(OP_ID)).thenReturn(Optional.of(scaOperation));
        when(scaOperation.getAuthCodeHash()).thenReturn("hash of object");
        when(hashGenerator.hash(any())).thenThrow(new HashGenerationException());

        scaOperationService.validateAuthCode(OP_ID, OP_DATA, "my tan");
    }
}
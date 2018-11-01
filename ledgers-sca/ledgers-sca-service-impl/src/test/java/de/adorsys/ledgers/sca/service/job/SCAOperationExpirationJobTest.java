package de.adorsys.ledgers.sca.service.job;

import de.adorsys.ledgers.sca.service.SCAOperationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SCAOperationExpirationJobTest {

    @InjectMocks
    private SCAOperationExpirationJob job;

    @Mock
    private SCAOperationService service;

    @Test
    public void checkOperationExpiration() {

        doNothing().when(service).processExpiredOperations();

        job.checkOperationExpiration();

        verify(service, times(1)).processExpiredOperations();
    }
}
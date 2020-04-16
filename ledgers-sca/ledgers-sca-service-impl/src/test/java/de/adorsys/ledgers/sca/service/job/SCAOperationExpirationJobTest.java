package de.adorsys.ledgers.sca.service.job;

import de.adorsys.ledgers.sca.service.SCAOperationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SCAOperationExpirationJobTest {

    @InjectMocks
    private SCAOperationExpirationJob job;

    @Mock
    private SCAOperationService service;

    @Test
    void checkOperationExpiration() {
        // Given
        doNothing().when(service).processExpiredOperations();

        // When
        job.checkOperationExpiration();

        // Then
        verify(service, times(1)).processExpiredOperations();
    }
}
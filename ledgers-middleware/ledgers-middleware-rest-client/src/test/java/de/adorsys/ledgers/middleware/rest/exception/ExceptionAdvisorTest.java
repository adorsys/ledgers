package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.util.exception.PostingErrorCode;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;


@ExtendWith(MockitoExtension.class)
class ExceptionAdvisorTest {

    private static final String DEV_ERROR_MSG = "Some error message";
    @InjectMocks
    private ExceptionAdvisor advisor;

    @Test
    void handlePostingModuleException() {
        // Given
        PostingModuleException exception = PostingModuleException.builder()
                                                   .errorCode(PostingErrorCode.LEDGER_NOT_FOUND)
                                                   .devMsg(DEV_ERROR_MSG)
                                                   .build();

        // When
        ResponseEntity<Map<String,String>> result = advisor.handlePostingModuleException(exception);

        // Then
        assertSame(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertSame(null, result.getBody().get("message"));
        assertSame(DEV_ERROR_MSG, result.getBody().get("devMessage"));
    }
}

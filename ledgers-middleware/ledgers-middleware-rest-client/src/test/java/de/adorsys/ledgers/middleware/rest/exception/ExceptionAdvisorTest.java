package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.postings.api.exception.PostingErrorCode;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionAdvisorTest {
    private static final String DEV_ERROR_MSG = "Some error message";
    @InjectMocks
    private ExceptionAdvisor advisor;

    @Test
    public void handlePostingModuleException() {
        PostingModuleException exception = PostingModuleException.builder()
                                                   .errorCode(PostingErrorCode.LEDGER_NOT_FOUND)
                                                   .devMsg(DEV_ERROR_MSG)
                                                   .build();
        ResponseEntity<Map> result = advisor.handlePostingModuleException(exception);

        assertSame(result.getStatusCode(), HttpStatus.NOT_FOUND);
        assertSame(result.getBody().get("message"), null);
        assertSame(result.getBody().get("devMessage"), DEV_ERROR_MSG);
    }
}

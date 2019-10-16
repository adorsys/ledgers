package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.util.exception.PostingErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class PostingHttpStatusResolver {
    private static final EnumMap<PostingErrorCode, HttpStatus> container = new EnumMap<>(PostingErrorCode.class);

    private PostingHttpStatusResolver() {}

    static {
        //404 Block
        container.put(LEDGER_ACCOUNT_NOT_FOUND, NOT_FOUND);
        container.put(LEDGER_NOT_FOUND, NOT_FOUND);
        container.put(POSTING_NOT_FOUND, NOT_FOUND);
        container.put(CHART_OF_ACCOUNT_NOT_FOUND, NOT_FOUND);

        //400 Block
        container.put(DOBLE_ENTRY_ERROR, BAD_REQUEST);
        container.put(BASE_LINE_TIME_ERROR, BAD_REQUEST);
        container.put(POSTING_TIME_MISSING, BAD_REQUEST);
        container.put(NOT_ENOUGH_INFO, BAD_REQUEST);
        container.put(NO_CATEGORY, BAD_REQUEST);
    }

    public static HttpStatus resolveHttpStatusByCode(PostingErrorCode code) {
        return container.get(code);
    }
}

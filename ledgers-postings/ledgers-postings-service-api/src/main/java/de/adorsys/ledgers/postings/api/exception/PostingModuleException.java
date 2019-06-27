package de.adorsys.ledgers.postings.api.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostingModuleException extends RuntimeException {
    private final PostingModuleErrorCode postingModuleErrorCode;
    private final String devMsg;
}

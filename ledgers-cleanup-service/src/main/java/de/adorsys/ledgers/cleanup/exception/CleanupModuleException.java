package de.adorsys.ledgers.cleanup.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CleanupModuleException extends Exception {
    private final String devMsg;
}

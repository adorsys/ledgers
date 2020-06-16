package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecoveryPointTO {
    private Long id;
    private String description;
    private LocalDateTime rollBackTime;
    private String branchId;
}

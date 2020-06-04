package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevertRequestTO {
    private String branchId;
    private LocalDateTime timestampToRevert;
}

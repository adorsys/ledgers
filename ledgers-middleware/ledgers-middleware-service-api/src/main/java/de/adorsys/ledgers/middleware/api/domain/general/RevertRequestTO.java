package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.Data;

@Data
public class RevertRequestTO {
    private String branchId;
    private long recoveryPointId;
}

package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class RecoveryPointTO {
    private Long id;
    private String description;
    private LocalDateTime rollBackTime;
    private String branchId;

    public RecoveryPointTO(String description) {
        this.description = description;
    }
}

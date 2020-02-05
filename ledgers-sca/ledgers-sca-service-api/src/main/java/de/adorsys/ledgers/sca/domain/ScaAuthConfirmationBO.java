package de.adorsys.ledgers.sca.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaAuthConfirmationBO {
    private boolean confirm;
    private OpTypeBO opTypeBO;
    private String opId;
}

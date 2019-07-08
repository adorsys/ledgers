package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO create core-api and remove this class https://git.adorsys.de/adorsys/xs2a/ledgers/issues/230
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaInfoTO {
    private String userId;
    private String scaId;
    private String authorisationId;
    private UserRoleTO userRole;
    private String scaMethodId;
    private String authCode;
    private TokenUsageTO tokenUsage;
    private String userLogin;
}

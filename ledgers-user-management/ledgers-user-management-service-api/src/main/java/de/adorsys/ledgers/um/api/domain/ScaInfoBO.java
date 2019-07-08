package de.adorsys.ledgers.um.api.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScaInfoBO {
    private String userId;
    private String scaId;
    private String authorisationId;
    private UserRoleBO userRole;
    private String scaMethodId;
    private String authCode;
    private TokenUsageBO tokenUsage;
    private String userLogin;

    public ScaInfoBO(String userId, String scaId, String authorisationId, UserRoleBO userRole) {
        this(userId, scaId, authorisationId, userRole, null, null, null, null);
    }
}

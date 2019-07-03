package de.adorsys.ledgers.um.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.*;

@Data
public class AccessTokenBO {

    private String sub;

    private String jti;

    private String login;

    private AisConsentBO consent;

    @JsonProperty("account_accesses")
    private List<AccountAccessBO> accountAccesses = new ArrayList<>();

    private UserRoleBO role;

    private Date iat;

    private Date exp;

    private Map<String, String> act = new HashMap<>();

    @JsonProperty("sca_id")
    private String scaId;

    @JsonProperty("authorisation_id")
    private String authorisationId;

    @JsonProperty("token_usage")
    private TokenUsageBO tokenUsage;

    @JsonIgnore
    public ScaInfoBO buildScaInfoBO() {
		return new ScaInfoBO(sub, scaId, authorisationId, role);
    }
}

/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class AccessTokenTO {
    /*
     * The subject, see rfc7519. This is generally the id of
     * the resource owner.
     */
    @Schema(description = "The database id of the initiator of this token")
    private String sub;

    /*
     * The unique token identifier, see rfc7519
     */
    @Schema(description = "The token identifier")
    private String jti;

    /*
     * The login name of the resource owner (user) on behalf of the holder of this token act.
     * Correspond to the pusId in psd2.
     */
    @Schema(description = "The login name of the initiator of this token")
    private String login;

    /*
     * Consent given by the bearer to the holder of this token.
     */
    @Schema(description = "The specification of psd2 account access permission associated with this token")
    private AisConsentTO consent;

    /*
     * The associated with the token.
     */
    @Schema(description = "Role to be inforced when this token is presented.")
    private UserRoleTO role;

    /*
     * issued at. see rfc7519
     */
    @Schema(description = "Issue time")
    private Date iat;

    /*
     * Expiration. see rfc7519
     */
    @Schema(description = "expiration time")
    private Date exp;

    @Schema(description = "The bearer this token.")
    private Map<String, String> act = new HashMap<>();

    @Schema(description = "The id of the sca object: login, payment, account access")
    @JsonProperty("sca_id")
    private String scaId;

    @Schema(description = "The last authorisation id leading to this token")
    @JsonProperty("authorisation_id")
    private String authorisationId;

    @Schema(description = "The usage of this token.")
    @JsonProperty("token_usage")
    private TokenUsageTO tokenUsage;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scopes")
    private Set<String> scopes;

    @JsonIgnore
    public boolean hasValidConsent() {
        return consent != null && consent.isValidConsent();
    }
}

/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.token.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Optional.ofNullable;
import static org.keycloak.common.util.Time.currentTime;

/**
 * @author Lorent Lempereur
 */
public class TokenConfiguration {

    @JsonProperty("tokenLifespanInSeconds")
    private Integer tokenLifespanInSeconds;

    @JsonProperty("scope")
    private String scope;

    public Integer getTokenLifespanInSeconds() {
        return tokenLifespanInSeconds;
    }

    public void setTokenLifespanInSeconds(Integer tokenLifespanInSeconds) {
        this.tokenLifespanInSeconds = tokenLifespanInSeconds;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int computeTokenExpiration(int maxExpiration, boolean longLivedTokenAllowed) {
        return ofNullable(tokenLifespanInSeconds)
                       .map(lifespan -> currentTime() + lifespan)
                       .map(requestedExpiration -> longLivedTokenAllowed ? requestedExpiration : Math.min(maxExpiration, requestedExpiration))
                       .orElse(maxExpiration);
    }
}

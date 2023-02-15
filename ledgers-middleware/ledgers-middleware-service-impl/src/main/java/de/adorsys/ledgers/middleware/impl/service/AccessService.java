/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.domain.Constants.*;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final UserService userService;
    private final KeycloakTokenService tokenService;

    @Value("${ledgers.sca.final.weight:100}")
    private int finalWeight;

    @Value("${ledgers.token.lifetime.seconds.sca:10800}")
    private int scaTokenLifeTime;
    @Value("${ledgers.token.lifetime.seconds.full:7776000}")
    private int fullTokenLifeTime;
    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void updateAccountAccessNewAccount(DepositAccountBO createdAccount, UserBO user, Integer scaWeight, AccessTypeTO accessType) {
        accessType = Optional.ofNullable(accessType).orElse(AccessTypeTO.OWNER);
        scaWeight = Optional.ofNullable(scaWeight).orElse(finalWeight);
        AccountAccessBO accountAccess = new AccountAccessBO(createdAccount.getIban(), createdAccount.getCurrency(), createdAccount.getId(), scaWeight, AccessTypeBO.valueOf(accessType.name()));
        updateAccountAccess(user, accountAccess);
        //Check account is created for a User who is part of a Branch and if so add access to the branch
        if (StringUtils.isNotBlank(user.getBranch())) {
            UserBO branch = userService.findById(user.getBranch());
            updateAccountAccess(branch, accountAccess.setWeight(finalWeight));
        }
    }

    private void updateAccountAccess(UserBO user, AccountAccessBO access) {
        if (user.hasAccessToAccountWithId(access.getAccountId())) {
            user.updateExistingAccess(access);
        } else {
            user.addNewAccess(access);
        }
        userService.updateAccountAccess(user.getLogin(), user.getAccountAccesses());
    }

    public BearerTokenTO exchangeTokenStartSca(boolean scaRequired, String accessToken) {
        return scaRequired
                       ? tokenService.exchangeToken(accessToken, scaTokenLifeTime, SCOPE_SCA)
                       : tokenService.exchangeToken(accessToken, fullTokenLifeTime, SCOPE_FULL_ACCESS);
    }

    public BearerTokenTO exchangeTokenEndSca(boolean authenticationCompleted, String accessToken) {
        String scope = multilevelScaEnable && !authenticationCompleted
                               ? SCOPE_PARTIAL_ACCESS
                               : SCOPE_FULL_ACCESS;
        return tokenService.exchangeToken(accessToken, fullTokenLifeTime, scope);
    }
}

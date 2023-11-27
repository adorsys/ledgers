/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRedirectScaService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.domain.Constants.*;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.SYSTEM;

@Component
@SuppressWarnings("PMD.TooManyMethods")
public class AccountAccessSecurityFilter extends SecurityExpressionAdapter {

    public AccountAccessSecurityFilter(Authentication authentication, MiddlewareAccountManagementService accountService,
                                       MiddlewarePaymentService paymentService, KeycloakAuthMapper authMapper,
                                       MiddlewareUserManagementService userManagementService,
                                       MiddlewareRedirectScaService scaService) {
        super(authentication, accountService, paymentService, userManagementService, authMapper, scaService);
    }

    //-- Account Related SYSTEM & STAFF checks --//
    public boolean isNewStaffUser(UserTO user) {
        return CollectionUtils.isNotEmpty(user.getUserRoles()) && user.getUserRoles().contains(SYSTEM)
                       || userManagementService.countUsersByBranch(user.getId()) == 0;
    }

    public boolean hasManagerAccessToAccountIban(String iban) {
        UserTO user = user();
        return hasAnyRole(SYSTEM.name(), STAFF.name()) && user.isEnabled() && hasManagerAccessIban(iban, user);
    }

    public boolean hasManagerAccessToAccountId(String accountId) {
        UserTO user = user();
        return hasAnyRole(SYSTEM.name(), STAFF.name()) && user.isEnabled() && hasManagerAccessId(accountId, user);
    }

    //-- Manager User checks --//
    public boolean hasManagerAccessToUser(String userId) {
        UserTO user = user();
        return hasAnyRole(SYSTEM.name(), STAFF.name()) && user.isEnabled() && hasAccessToUser(user, userId);
    }

    public boolean isSameUser(String userId) {
        return user().getId().equals(userId);
    }

    //--General Payment checks --//
    public boolean hasAccessToAccountByPaymentId(String paymentId) {
        return hasAccessToAccount(getAccountIdFromPayment(paymentId));
    }

    //-- General Account checks --//
    public boolean hasAccessToAccountsWithIbans(Collection<String> ibans) {
        UserTO user = user();
        return user.getUserRoles().contains(SYSTEM)
                       || user.getUserRoles().contains(STAFF) && user.hasAccessToAccountsWithIbans(ibans)
                       || user.hasAccessToAccountsWithIbans(ibans) && ibans.stream().allMatch(this::isEnabledAccountIban);
    }

    //System retrieves regardless of status, STAFF & CUSTOMER if has access, CUSTOMER if accountEnabled
    public boolean hasAccessToAccount(String accountId) {
        UserTO user = user();
        return user.getUserRoles().contains(SYSTEM)
                       || user.getUserRoles().contains(STAFF) && user.hasAccessToAccountWithId(accountId)
                       || user.hasAccessToAccountWithId(accountId) && isEnabledAccount(accountId);
    }

    public boolean hasAccessToAccountWithIban(String iban) {
        UserTO user = user();
        return user.getUserRoles().contains(SYSTEM)
                       || user.getUserRoles().contains(STAFF) && user.hasAccessToAccountWithIban(iban)
                       || user.hasAccessToAccountWithIban(iban) && isEnabledAccountIban(iban);
    }

    public boolean accountInfoByIdentifier(AccountIdentifierTypeTO type, String accountIdentifier) {
        return type == AccountIdentifierTypeTO.IBAN
                       ? hasAccessToAccountWithIban(accountIdentifier)
                       : hasAccessToAccount(accountIdentifier);
    }

    public boolean isEnabledAccount(String accountId) {
        return accountService.getDepositAccountById(accountId, LocalDateTime.now(), false).isEnabled();
    }

    public boolean hasAccessToAccountByLogin(String login, String iban) {
        return userManagementService.findByUserLogin(login).hasAccessToAccountWithIban(iban);
    }

    public boolean hasAccessToAccountsByLogin(String login, List<AccountReferenceTO> references) {
        Set<String> ibans = references.stream().map(AccountReferenceTO::getIban).collect(Collectors.toSet());
        return userManagementService.findByUserLogin(login).hasAccessToAccountsWithIbans(ibans);
    }

    //-- General User checks --//
    public boolean isEnabledUser(String userId) {
        return userManagementService.findById(userId).isEnabled();
    }

    //-- Scope Related checks --//
    public boolean hasScaScope() {
        return hasAnyScope(SCOPE_SCA, SCOPE_PARTIAL_ACCESS, SCOPE_FULL_ACCESS);
    }

    public boolean hasPartialScope() {
        return hasAnyScope(SCOPE_PARTIAL_ACCESS, SCOPE_FULL_ACCESS);
    }

    public boolean hasAccessToAccountByScaOperation(StartScaOprTO opr) {
        return EnumSet.of(OpTypeTO.PAYMENT, OpTypeTO.CANCEL_PAYMENT).contains(opr.getOpType())
                       ? hasAccessToAccountByPaymentId(opr.getOprId())
                       : hasAccessToAccountsWithIbans(accountService.getAccountsFromConsent(opr.getOprId()));
    }

    public boolean hasAccessToAccountByAuthorizationId(String authorizationId) {
        return hasAccessToAccountByScaOperation(scaService.loadScaInformation(authorizationId));
    }

    private AccessTokenTO getAccessTokenTO() {
        Jwt credentials = (Jwt) getAuthentication().getCredentials();
        return authMapper.toAccessTokenFromJwt(credentials);
    }

    private UserTO user() {
        return userManagementService.findByUserLogin(getAccessTokenTO().getLogin());
    }

    private boolean hasAnyScope(String... scopes) {
        Set<String> scopesInToken = getScopes();
        return Arrays.stream(scopes)
                       .anyMatch(scopesInToken::contains);
    }

    private Set<String> getScopes() {
        Jwt credentials = (Jwt) getAuthentication().getCredentials();
        return new HashSet<>(Arrays.asList(credentials.getClaimAsString("scope").split(" ")));
    }

    private boolean isEnabledAccountIban(String iban) {
        return accountService.getAccountsByIbanAndCurrency(iban, "").stream().allMatch(AccountDetailsTO::isEnabled);
    }

    private String getAccountIdFromPayment(String paymentId) {
        return paymentService.getPaymentById(paymentId).getAccountId();
    }

    private boolean hasAccessToUser(UserTO initiator, String userId) {
        return !initiator.getUserRoles().contains(STAFF) || userManagementService.findById(userId).getBranch().equals(initiator.getId());
    }

    private boolean hasManagerAccessIban(String iban, UserTO user) {
        return user.getUserRoles().contains(SYSTEM) || user.hasAccessToAccountWithIban(iban);
    }

    private boolean hasManagerAccessId(String accountId, UserTO user) {
        return user.getUserRoles().contains(SYSTEM) || user.hasAccessToAccountWithId(accountId);
    }
}

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountUncheckedException;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AccessService {
    private static final String ERROR_MESSAGE_USER_NF = "Can not find user with id %s. But this user is supposed to exist.";

    private final UserService userService;
    private final AccessTokenTO accessToken;

    public AccessService(UserService userService, AccessTokenTO accessToken) {
        this.userService = userService;
        this.accessToken = accessToken;
    }

    public void addAccess(List<AccountAccessTO> accountAccess, DepositAccountBO depositAccountBO,
                          final Map<String, UserBO> persistBuffer) throws UserNotFoundException {
        for (AccountAccessTO accountAccessTO : accountAccess) {
            // TODO: check if accountAccess User is not Null
            UserBO user = persistBuffer.get(accountAccessTO.getUser().getId());
            if (user == null) {
                user = userService.findById(accountAccessTO.getUser().getId());
            }
            AccountAccessBO accountAccessBO = new AccountAccessBO(depositAccountBO.getIban(),
                                                                  AccessTypeBO.valueOf(accountAccessTO.getAccessType().name()));
            addAccess(user, accountAccessBO, persistBuffer);
        }
        for (UserBO u : persistBuffer.values()) {
            userService.updateAccountAccess(u.getLogin(), u.getAccountAccesses());
        }
    }

    public void addAccess(final UserBO user, AccountAccessBO accountAccessBO, final Map<String, UserBO> persistBuffer) {
        AccountAccessBO existingAac = user.getAccountAccesses().stream().filter(a -> a.getIban().equals(accountAccessBO.getIban()))
                                              .findFirst()
                                              .orElseGet(() -> {
                                                  AccountAccessBO aac = new AccountAccessBO();
                                                  aac.setAccessType(accountAccessBO.getAccessType());
                                                  aac.setIban(accountAccessBO.getIban());
                                                  user.getAccountAccesses().add(aac);
                                                  persistBuffer.put(user.getId(), user);
                                                  return aac;
                                              });
        if (existingAac.getId() == null && existingAac.getAccessType().equals(accountAccessBO.getAccessType())) {
            existingAac.setAccessType(accountAccessBO.getAccessType());
            persistBuffer.put(user.getId(), user);
        }
    }

    public UserBO loadCurrentUser() {
        // Load owner
        UserBO userBo;
        try {
            userBo = userService.findById(accessToken.getSub());
        } catch (UserNotFoundException e) {
            throw new DepositAccountUncheckedException(String.format(ERROR_MESSAGE_USER_NF, accessToken.getSub()), e);
        }
        return userBo;
    }


    public List<String> filterOwnedAccounts(List<AccountAccessTO> accountAccesses) {
        // All iban owned by this user.
        //TODO should be moved to UM @dmiex
        return accountAccesses == null
                       ? Collections.emptyList()
                       : accountAccesses.stream()
                                 .filter(a -> a.getAccessType() != null && AccessTypeBO.OWNER.name().equals(a.getAccessType().name()))
                                 .map(AccountAccessTO::getIban)
                                 .collect(Collectors.toList());
    }


    public AccountAccessTO createAccountAccess(String accNbr, UserTO userTO) {
        AccountAccessTO accountAccess = new AccountAccessTO();
        accountAccess.setAccessType(AccessTypeTO.OWNER);
        accountAccess.setIban(accNbr);
        accountAccess.setUser(userTO);
        return accountAccess;
    }

    public LocalDateTime getTimeAtEndOfTheDay(LocalDate date) {
        return date.atTime(23, 59, 59, 99);
    }

    public int resolveScaWeightByDebtorAccount(List<AccountAccessBO> accountAccesses, String debtorAccount) {
        return accountAccesses.stream()
                       .filter(ac -> StringUtils.equalsIgnoreCase(ac.getIban(), debtorAccount))
                       .map(AccountAccessBO::getScaWeight)
                       .findFirst()
                       .orElse(0);
    }

    public int resolveMinimalScaWeightForConsent(AisAccountAccessInfoBO access, List<AccountAccessBO> accountAccesses) {
        Set<String> combinedAccounts = Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                                               .flatMap(Collection::stream)
                                               .collect(Collectors.toSet());

        return accountAccesses.stream()
                       .filter(ac -> combinedAccounts.contains(ac.getIban()))
                       .min(Comparator.comparing(AccountAccessBO::getScaWeight))
                       .map(AccountAccessBO::getScaWeight)
                       .orElse(0);
    }
}

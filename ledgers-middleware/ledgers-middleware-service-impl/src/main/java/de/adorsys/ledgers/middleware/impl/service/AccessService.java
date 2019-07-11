package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final UserService userService;

    public void updateAccountAccess(UserBO user, AccountAccessBO access) {
        if (!containsAccess(user.getAccountAccesses(), access.getIban())) {
            user.getAccountAccesses().add(access);
        } else {
            user.getAccountAccesses().forEach(a -> {
                if (a.getIban().equals(access.getIban())) {
                    a.setAccessType(access.getAccessType());
                    a.setScaWeight(access.getScaWeight());
                }
            });
        }
        userService.updateAccountAccess(user.getLogin(), user.getAccountAccesses());
    }

    private boolean containsAccess(List<AccountAccessBO> accesses, String iban) {
        return accesses.stream()
                       .anyMatch(a -> a.getIban().equals(iban));
    }

    public UserBO loadCurrentUser(String userId) {
        // Load owner
        return userService.findById(userId);
    }

    public List<String> filterOwnedAccounts(List<AccountAccessTO> accountAccesses) {
        // All iban owned by this user.
        //TODO should be moved to UM @dmiex
        return accountAccesses == null
                       ? Collections.emptyList()
                       : accountAccesses.stream()
                                 .filter(a -> AccessTypeBO.OWNER.name().equals(a.getAccessType().name()))
                                 .map(AccountAccessTO::getIban)
                                 .collect(Collectors.toList());
    }

    public AccountAccessBO createAccountAccess(String accNbr, AccessTypeBO accessType) {
        AccountAccessBO accountAccess = new AccountAccessBO();
        accountAccess.setAccessType(accessType);
        accountAccess.setIban(accNbr);
        return accountAccess;
    }

    public LocalDateTime getTimeAtEndOfTheDay(LocalDate date) {
        return date.atTime(23, 59, 59, 99);
    }

    /**
     * Calculates sca weight using debtor iban and users account accesses for payment
     */
    public int resolveScaWeightByDebtorAccount(List<AccountAccessBO> accountAccesses, String debtorAccount) {
        return accountAccesses.stream()
                       .filter(ac -> StringUtils.equalsIgnoreCase(ac.getIban(), debtorAccount))
                       .map(AccountAccessBO::getScaWeight)
                       .findFirst()
                       .orElse(0);
    }

    /**
     * Calculates minimal sca weight for consent using list of account accesses from consent and user account accesses
     */
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

    public boolean userHasAccessToAccount(UserTO user, String iban) {
        return user.getAccountAccesses().stream()
                       .anyMatch(a -> a.getIban().equals(iban));
    }
}

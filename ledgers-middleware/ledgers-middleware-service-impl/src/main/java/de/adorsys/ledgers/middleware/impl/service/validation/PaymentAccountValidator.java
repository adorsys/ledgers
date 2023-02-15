/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException.blockedSupplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAccountValidator extends AbstractPaymentValidator {
    private final DepositAccountService accountService;

    @Override
    public void check(PaymentBO payment, UserBO user) {
        validateAccountsAndFillMissing(payment);
        fillDebtorNameIfMissing(user, payment);
        checkNext(payment, user);
    }

    /**
     * Performs check of Debtor account for presence in current Ledger instance, if currency is missing add it to payment
     * Adds debtorAccount id to payment
     * Checks if creditor accounts are present and if so checks currency
     * Checks if accounts are not blocked
     *
     * @param payment payment for validation
     */
    private void validateAccountsAndFillMissing(PaymentBO payment) {
        DepositAccountBO account = getCheckedAccount(payment);
        payment.updateDebtorAccountCurrency(account.getCurrency());
        payment.setAccountId(account.getId());
    }

    private void fillDebtorNameIfMissing(UserBO user, PaymentBO payment) {
        if (StringUtils.isBlank(payment.getDebtorName())) {
            payment.setDebtorName(user.getLogin());
        }
    }

    private DepositAccountBO getCheckedAccount(PaymentBO paymentBO) {
        paymentBO.getTargets()
                .forEach(t -> {
                    try {
                        DepositAccountBO acc = checkAccountStatusAndCurrencyMatch(t.getCreditorAccount(), false, t.getInstructedAmount().getCurrency());
                        t.setCreditorAccount(acc.getReference());
                    } catch (MiddlewareModuleException e) {
                        if (EnumSet.of(ACCOUNT_DISABLED, CURRENCY_MISMATCH).contains(e.getErrorCode())) {
                            log.error(e.getDevMsg());
                            throw e;
                        }
                    } catch (DepositModuleException e) {
                        log.info("Creditor account is located in another ASPSP");
                    }
                });
        return checkAccountStatusAndCurrencyMatch(paymentBO.getDebtorAccount(), true, null);
    }

    private DepositAccountBO checkAccountStatusAndCurrencyMatch(AccountReferenceBO reference, boolean isDebtor, Currency currency) {
        DepositAccountBO account = Optional.ofNullable(reference.getCurrency())
                                           .map(c -> accountService.getAccountByIbanAndCurrency(reference.getIban(), c))
                                           .orElseGet(() -> getAccountByIbanAndParamCurrencyErrorIfNotSingle(reference.getIban(), isDebtor, currency));
        if (!account.isEnabled()) {
            throw blockedSupplier(ACCOUNT_DISABLED, reference.getIban(), account.isBlocked()).get();
        }
        return account;
    }

    private DepositAccountBO getAccountByIbanAndParamCurrencyErrorIfNotSingle(String iban, boolean isDebtor, Currency currency) {
        List<DepositAccountBO> accounts = accountService.getAccountsByIbanAndParamCurrency(iban, "");
        if (CollectionUtils.isEmpty(accounts) && !isDebtor) {
            return DepositAccountBO.builder().iban(iban).currency(currency).creditLimit(BigDecimal.ZERO).build();
        }
        if (accounts.size() != 1) {
            String msg = CollectionUtils.isEmpty(accounts)
                                 ? String.format("Account with IBAN: %s Not Found!", iban)
                                 : String.format("Initiation of payment for Account with IBAN: %s is impossible as it is a Multi-Currency-Account. %nPlease specify currency for sub-account to proceed.", iban);
            MiddlewareErrorCode errorCode = CollectionUtils.isEmpty(accounts)
                                                    ? PAYMENT_VALIDATION_EXCEPTION
                                                    : CURRENCY_MISMATCH;
            throw MiddlewareModuleException.builder()
                          .errorCode(errorCode)
                          .devMsg(msg)
                          .build();
        }
        return accounts.iterator().next();
    }
}
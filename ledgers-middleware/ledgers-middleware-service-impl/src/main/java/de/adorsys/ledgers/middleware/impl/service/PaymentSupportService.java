package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class PaymentSupportService {  //TODO move to RequestValidationFilter
    private final PaymentProductsConfig paymentProductsConfig;
    private final DepositAccountService accountService;

    public void validatePayment(PaymentBO paymentBO) {
        String msg = null;
        if (!paymentBO.isValidAmount()) {
            msg = "Instructed amount is invalid.";
        }

        if (paymentProductsConfig.isNotSupportedPaymentProduct(paymentBO.getPaymentProduct())) {
            msg = "Payment Product not Supported!";
        }
        if (msg != null) {
            throw MiddlewareModuleException.builder()
                          .devMsg(String.format("Payment validation failed! %s", msg))
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .build();
        }
    }

    public DepositAccountBO getCheckedAccount(PaymentBO paymentBO) {
        DepositAccountBO debtorAccount = checkAccountStatusAndCurrencyMatch(paymentBO.getDebtorAccount(), true, null);
        paymentBO.setAccountId(debtorAccount.getId());
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
        return debtorAccount;
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
            return new DepositAccountBO(null, iban, null, null, null, null, currency, null, null, null, null, null, null, null, false, false, null, null, BigDecimal.ZERO);
        }
        if (accounts.size() != 1) {
            String msg = CollectionUtils.isEmpty(accounts)
                                 ? String.format("Account with IBAN: %s Not Found!", iban)
                                 : String.format("Initiation of payment for Account with IBAN: %s is impossible as it is a Multi-Currency-Account. %nPlease specify currency for sub-account to proceed.", iban);
            MiddlewareErrorCode errorCode = CollectionUtils.isEmpty(accounts)
                                                    ? PAYMENT_PROCESSING_FAILURE
                                                    : CURRENCY_MISMATCH;
            throw MiddlewareModuleException.builder()
                          .errorCode(errorCode)
                          .devMsg(msg)
                          .build();
        }
        return accounts.iterator().next();
    }
}
/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.app.mock.BulkPaymentsData;
import de.adorsys.ledgers.app.mock.MockbankInitData;
import de.adorsys.ledgers.app.mock.SinglePaymentsData;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTargetTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestTransactionService {
    private final MockbankInitData mockbankInitData;
    private final DepositAccountService depositAccountService;
    private final PaymentRestInitiationService restInitiationService;

    private static final String ACCOUNT_NOT_FOUND_MSG = "Account {} not Found! Should never happen while initiating mock data!";
    private static final String NO_USER_BY_IBAN = "Could not get User By Iban {}! Should never happen while initiating mock data!";
    private static final LocalDateTime START_DATE = LocalDateTime.of(2018, 1, 1, 1, 1);

    public void performTransactions() {
        List<UserTO> users = mockbankInitData.getUsers();
        performSinglePayments(users);
        performBulkPayments(users);
    }

    private void performSinglePayments(List<UserTO> users) {
        for (SinglePaymentsData paymentsData : mockbankInitData.getSinglePayments()) {
            PaymentTO payment = paymentsData.getSinglePayment();
            UserTO user = getUserByIban(users, payment.getDebtorAccount().getIban());
            payment.setDebtorName(user.getLogin());
            try {
                if (isAbsentTransactionRegular(payment.getDebtorAccount().getIban(), payment.getDebtorAccount().getCurrency(), payment.getTargets().get(0).getEndToEndIdentification())) {
                    restInitiationService.executePayment(user, payment);
                } else {
                    logPaymentPresentInfo(payment);
                }
            } catch (DepositModuleException e) {
                log.error(ACCOUNT_NOT_FOUND_MSG, payment.getDebtorAccount().getIban());
            } catch (UserManagementModuleException e) {
                log.error(NO_USER_BY_IBAN, payment.getDebtorAccount().getIban());
            }
        }
    }

    private void performBulkPayments(List<UserTO> users) {
        for (BulkPaymentsData paymentsData : mockbankInitData.getBulkPayments()) {
            PaymentTO payment = paymentsData.getBulkPayment();
            AccountReferenceTO debtorAccount = payment.getDebtorAccount();
            UserTO user = getUserByIban(users, debtorAccount.getIban());
            payment.setDebtorName(user.getLogin());
            try {
                boolean isAbsentTransaction = Optional.ofNullable(payment.getBatchBookingPreferred()).orElse(false)
                                                      ? isAbsentTransactionBatch(payment)
                                                      : isAbsentTransactionRegular(debtorAccount.getIban(), debtorAccount.getCurrency(), payment.getTargets().iterator().next().getEndToEndIdentification());

                if (isAbsentTransaction) {
                    restInitiationService.executePayment(user, payment);
                } else {
                    logPaymentPresentInfo(payment);
                }
            } catch (DepositModuleException e) {
                log.error(ACCOUNT_NOT_FOUND_MSG, debtorAccount.getIban());
            } catch (UserManagementModuleException e) {
                log.error(NO_USER_BY_IBAN, debtorAccount.getIban());
            }
        }
    }

    private void logPaymentPresentInfo(PaymentTO payment) {
        Currency currency = payment.getTargets().iterator().next().getInstructedAmount().getCurrency();
        BigDecimal amount = payment.getTargets().stream()
                                    .map(PaymentTargetTO::getInstructedAmount)
                                    .map(AmountTO::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        log.info("{} Payment from {} for {} {} is already present", payment.getPaymentType(), payment.getDebtorName(), amount, currency);
    }

    private boolean isAbsentTransactionBatch(PaymentTO payment) {
        DepositAccountBO account = depositAccountService.getAccountByIbanAndCurrency(payment.getDebtorAccount().getIban(), payment.getDebtorAccount().getCurrency());
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getId(), START_DATE, LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO.subtract(payment.getTargets().stream()
                                                            .map(PaymentTargetTO::getInstructedAmount)
                                                            .map(AmountTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                                                            .setScale(2, RoundingMode.HALF_UP));
        return transactions.stream()
                       .noneMatch(t -> t.getTransactionAmount().getAmount().equals(total));
    }

    private boolean isAbsentTransactionRegular(String iban, Currency currency, String entToEndId) {
        DepositAccountBO account = depositAccountService.getAccountByIbanAndCurrency(iban, currency);
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getId(), START_DATE, LocalDateTime.now());
        return transactions.stream()
                       .noneMatch(t -> entToEndId.equals(t.getEndToEndId()));
    }

    private UserTO getUserByIban(List<UserTO> users, String iban) {
        return users.stream()
                       .filter(u -> u.hasAccessToAccountWithIban(iban))
                       .findFirst()
                       .orElseThrow(() -> UserManagementModuleException.builder().build());
    }


}

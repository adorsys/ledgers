package de.adorsys.ledgers.middleware.impl.policies;

import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.NO_SUCH_ALGORITHM;

public class PaymentCoreDataPolicyHelper {
    private static final DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private PaymentCoreDataPolicyHelper() {
    }

    public static PaymentCoreDataTO getPaymentCoreDataInternal(PaymentBO payment) {
        try {
            PaymentCoreDataTO data = new PaymentCoreDataTO();
            data.setPaymentType(payment.getPaymentType().name());
            data.setPaymentId(payment.getPaymentId());
            checkPaymentType(payment);
            if (EnumSet.of(SINGLE, PERIODIC).contains(payment.getPaymentType())) {
                updateSingleTargetedPaymentData(payment, data);
            } else {
                updateBulkTargetedPaymentData(payment, data);
            }
            data.setRequestedExecutionDate(resolveExecutionDate(payment.getRequestedExecutionDate()));

            return data;
        } catch (NoSuchAlgorithmException e) {
            throw MiddlewareModuleException.builder()
                          .errorCode(NO_SUCH_ALGORITHM)
                          .devMsg("INTERNAL ERROR, A MESSAGE HAS BEING SENT TO THE BANK ADMINISTRATION TO FIX THIS ISSUE. WE ARE SORRY FOR TEMPORARY INCONVENIENCES.")
                          .build();
        }
    }

    private static void updateBulkTargetedPaymentData(PaymentBO payment, PaymentCoreDataTO data) throws NoSuchAlgorithmException {
        List<PaymentTargetBO> targets = payment.getTargets();
        // Bulk
        data.setPaymentsSize("" + targets.size());
        data.setCreditorName("Many Recipients");
        // Hash of all receiving Iban
        MessageDigest md = MessageDigest.getInstance("MD5");
        BigDecimal amt = BigDecimal.ZERO;
        if (currenciesMatch(targets)) {
            data.setCurrency(targets.iterator().next().getInstructedAmount().getCurrency().getCurrencyCode());
            for (PaymentTargetBO t : targets) {
                md.update(t.getCreditorAccount().getIban().getBytes(StandardCharsets.UTF_8));
                amt = amt.add(t.getInstructedAmount().getAmount());
            }
            data.setAmount(formatAmount(amt));
        } else {
            data.setCurrency("Multi-currency payment");
            data.setAmount("The amount would be calculated upon payment processing due to exchange rate diffs");

        }
        data.setCreditorIban(DatatypeConverter.printHexBinary(md.digest()));
    }

    private static boolean currenciesMatch(List<PaymentTargetBO> targets) {
        return targets.stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .map(AmountBO::getCurrency)
                       .allMatch(c -> c == targets.iterator().next().getInstructedAmount().getCurrency());
    }

    private static void updateSingleTargetedPaymentData(PaymentBO payment, PaymentCoreDataTO data) {
        PaymentTargetBO t = payment.getTargets().iterator().next();
        data.setCreditorIban(t.getCreditorAccount().getIban());
        data.setCreditorName(t.getCreditorName());
        if (t.getInstructedAmount().getCurrency() != null) {
            data.setCurrency(t.getInstructedAmount().getCurrency().getCurrencyCode());
        } else if (payment.getDebtorAccount().getCurrency() != null) {
            data.setCurrency(payment.getDebtorAccount().getCurrency().getCurrencyCode());
        }
        data.setAmount(formatAmount(t.getInstructedAmount().getAmount()));
        data.setPaymentProduct(payment.getPaymentProduct());
        if (PERIODIC.equals(payment.getPaymentType())) {
            data.setDayOfExecution("" + payment.getDayOfExecution());
            data.setExecutionRule(payment.getExecutionRule());
            data.setFrequency("" + payment.getFrequency());
        }
    }

    private static void checkPaymentType(PaymentBO payment) {
        if (isIncompatiblePaymentType(payment)) {
            throw MiddlewareModuleException.builder()
                          .devMsg("Malformed payment body, incompatible payment type")
                          .errorCode(MiddlewareErrorCode.PAYMENT_PROCESSING_FAILURE)
                          .build();
        }
    }

    private static boolean isIncompatiblePaymentType(PaymentBO payment) {
        return BULK == payment.getPaymentType() && payment.getTargets().size() < 2
                       || EnumSet.of(SINGLE, PERIODIC).contains(payment.getPaymentType()) && payment.getTargets().size() != 1;
    }

    private static String resolveExecutionDate(LocalDate requestedExecutionDate) {
        return Optional.ofNullable(requestedExecutionDate)
                       .orElseGet(LocalDate::now)
                       .format(formatter);
    }

    private static String formatAmount(BigDecimal amount) {
        return decimalFormat.format(amount);
    }
}

package de.adorsys.ledgers.middleware.impl.policies;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.CURRENCY_MISMATCH;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.NO_SUCH_ALGORITHM;

public class PaymentCoreDataPolicyHelper {
    private static final DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public static PaymentCoreDataTO getPaymentCoreDataInternal(PaymentBO r) {
        try {
            PaymentCoreDataTO p = new PaymentCoreDataTO();
            p.setPaymentType(r.getPaymentType().name());
            p.setPaymentId(r.getPaymentId());
            if (r.getTargets().size() == 1) {// Single, Periodic, Future Dated
                PaymentTargetBO t = r.getTargets().iterator().next();
                p.setCreditorIban(t.getCreditorAccount().getIban());
                p.setCreditorName(t.getCreditorName());
                if (t.getInstructedAmount().getCurrency() != null) {
                    p.setCurrency(t.getInstructedAmount().getCurrency().getCurrencyCode());
                } else if (r.getDebtorAccount().getCurrency() != null) {
                    p.setCurrency(r.getDebtorAccount().getCurrency().getCurrencyCode());
                }
                p.setAmount(formatAmount(t.getInstructedAmount().getAmount()));
                setPaymentProduct(p, t);
            } else {
                List<PaymentTargetBO> targets = r.getTargets();
                if (!targets.isEmpty()) {
                    setPaymentProduct(p, targets.iterator().next());
                }
                // Bulk
                p.setPaymentsSize("" + targets.size());
                p.setCreditorName("Many Receipients");
                // Hash of all receiving Iban
                MessageDigest md = MessageDigest.getInstance("MD5");
                BigDecimal amt = BigDecimal.ZERO;
                for (PaymentTargetBO t : targets) {
                    if (p.getCurrency() != null && !p.getCurrency().equals(t.getInstructedAmount().getCurrency().getCurrencyCode())) {
                        throw MiddlewareModuleException.builder()
                                .errorCode(CURRENCY_MISMATCH)
                                .devMsg(String.format("Currency mismatched in bulk payment with id %s", r.getPaymentId()))
                                .build();
                    }
                    p.setCurrency(t.getInstructedAmount().getCurrency().getCurrencyCode());
                    md.update(t.getCreditorAccount().getIban().getBytes(StandardCharsets.UTF_8));
                    amt = amt.add(t.getInstructedAmount().getAmount());
                }
                p.setAmount(formatAmount(amt));
                p.setCreditorIban(DatatypeConverter.printHexBinary(md.digest()));
            }

            p.setRequestedExecutionDate(resolveExecutionDate(r.getRequestedExecutionDate()));

            if (PaymentTypeBO.PERIODIC.equals(r.getPaymentType())) {
                p.setDayOfExecution("" + r.getDayOfExecution());
                p.setExecutionRule(r.getExecutionRule());
                p.setFrequency("" + r.getFrequency());
            }
            return p;
        } catch (NoSuchAlgorithmException e) {
            throw MiddlewareModuleException.builder()
                    .errorCode(NO_SUCH_ALGORITHM)
                    .devMsg("INTERNAL ERROR, A MESSAGE HAS BEING SENT TO THE BANK ADMINISTRATION TO FIX THIS ISSUE. WE ARE SORRY FOR TEMPORARY INCONVENIENCES.")
                    .build();
        }
    }

    private static String resolveExecutionDate(LocalDate requestedExecutionDate) {
        return Optional.ofNullable(requestedExecutionDate)
                .orElseGet(LocalDate::now)
                .format(formatter);
    }

    private static void setPaymentProduct(PaymentCoreDataTO p, PaymentTargetBO t) {
        String paymentProduct = t.getPaymentProduct() == null
                ? null
                : t.getPaymentProduct().getValue();
        p.setPaymentProduct(paymentProduct);
    }

    private static String formatAmount(BigDecimal amount) {
        return decimalFormat.format(amount);
    }
}

package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.AddressBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import de.adorsys.ledgers.um.api.domain.UserBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.BULK;
import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.PERIODIC;
import static de.adorsys.ledgers.middleware.impl.service.validation.PaymentFieldValidator.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSyntacticalValidator extends AbstractPaymentValidator {
    private final PaymentProductsConfig paymentProductsConfig;

    @Value("${ledgers.payment_validator.allow_past_dates:true}")
    private boolean allowDatesInThePast;

    @Value("${ledgers.payment_validator.allow_same_end_to_end_ids:true}")
    private boolean allowSameEndToEndIds;

    @Override
    public void check(PaymentBO payment, UserBO user) {
        checkDebtorPart(payment);
        checkCreditorParts(payment);
        checkPaymentTypeRelatedFields(payment);
        checkNext(payment, user);
    }

    private void checkDebtorPart(PaymentBO payment) {
        errorIf(payment.getPaymentType() == null, "PaymentType is required!");
        shouldBePresent(payment::getPaymentProduct, "PaymentProduct");
        errorIf(paymentProductsConfig.isNotSupportedPaymentProduct(payment.getPaymentProduct()), "Payment Product not Supported!");
        errorIf(isInvalidStartingTransactionStatus(payment), "Invalid transactionStatus for initiation!");
        errorIf(payment.getDebtorAccount() == null, "DebtorAccount should be present!");
        errorIf(payment.getDebtorAccount().isInvalidReference(), "Malformed debtorAccount!");
        errorIf(!isValidAmount(payment), "Amount can not be negative!");
        payment.getTargets().forEach(t -> shouldBePresent(t::getEndToEndIdentification, "EndToEndIdentification"));
        errorIf(isInvalidEndToEndIds(payment, allowSameEndToEndIds), "EndToEndIdentification's should be unique!");
        //payment.getDebtorAgent(); N/A
    }

    private void checkCreditorParts(PaymentBO payment) {
        errorIf(CollectionUtils.isEmpty(payment.getTargets()), "Payment targets are absent!");
        payment.getTargets().forEach(this::validateTarget);
    }

    private void checkPaymentTypeRelatedFields(PaymentBO payment) {
        errorIf(BooleanUtils.isTrue(payment.getBatchBookingPreferred()) && BULK != payment.getPaymentType(),
                "BatchBooking is only valid for Bulk payments!");
        if (PERIODIC == payment.getPaymentType()) {
            shouldBeNull(payment::getRequestedExecutionDate, "requestedExecutionDate");
            shouldBeNull(payment::getRequestedExecutionTime, "requestedExecutionTime");
            errorIf(isInvalidStartDate(payment, allowDatesInThePast), "Invalid startDate!");
            errorIf(isInvalidEndDate(payment), "Invalid endDate! End date should be after startDate!");
            errorIf(isInvalidExecutionRule(payment), "Invalid executionRule!");
            errorIf(payment.getFrequency() == null, "FrequencyCode is mandated for Periodic Payments!");
            errorIf(isInvalidExecutionDay(payment), "Incorrect dayOfExecution!");
        } else {
            errorIf(isInvalidRequestedExecutionDateTime(payment, allowDatesInThePast), "requestedExecutionDate/Time can not be in the past!");
            shouldBeNull(payment::getStartDate, "startDate");
            shouldBeNull(payment::getEndDate, "endDate");
            shouldBeNull(payment::getExecutionRule, "executionRule");
            shouldBeNull(payment::getFrequency, "frequency");
            shouldBeNull(payment::getDayOfExecution, "dayOfExecution");
        }
    }

    private void validateTarget(PaymentTargetBO target) {
        shouldBePresent(target::getCreditorName, "CreditorName");
        shouldNotExceed(target::getCreditorName, 70, "CreditorName");
        errorIf(target.getCreditorAccount().isInvalidReference(), "Malformed creditorAccount for " + target.getEndToEndIdentification());

        validateAddress(target.getCreditorAddress());
        shouldNotExceed(target::getRemittanceInformationUnstructured, 140, "RemittanceInformationUnstructured");

        //target.getCreditorAgent(); target.getChargeBearer(); target.getPurposeCode(); target.getRemittanceInformationStructured(); N/A
    }

    private void validateAddress(AddressBO address) {
        errorIf(address == null, "CreditorAddress is a required field!");
        shouldNotExceed(address::getStreet, 70, "Street");
        shouldBePresent(address::getCountry, "Country");
    }

    private void shouldBeNull(Supplier<Object> supplier, String fieldName) {
        errorIf(supplier.get() != null, fieldName + " is forbidden for current payment type!");
    }

    private void shouldBePresent(Supplier<String> source, String fieldName) {
        errorIf(StringUtils.isBlank(source.get()), fieldName + " is Required!");
    }

    private void shouldNotExceed(Supplier<String> source, int maxLength, String fieldName) {
        errorIf(StringUtils.isNotBlank(source.get()) && source.get().length() > maxLength, fieldName + " exceeds maximum length!");
    }

    private void errorIf(boolean predicateInvalid, String msg) {
        if (predicateInvalid) {
            log.error(msg);
            throw MiddlewareModuleException.paymentValidationException(msg);
        }
    }
}

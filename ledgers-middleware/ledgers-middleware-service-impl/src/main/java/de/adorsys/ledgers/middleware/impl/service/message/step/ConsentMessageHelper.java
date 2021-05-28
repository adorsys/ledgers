package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO.ALL_ACCOUNTS;
import static de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES;

@AllArgsConstructor
public class ConsentMessageHelper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

    private final AisConsentBO consent;

    public String template() {
        checkNullConsent();
        // Deleting the consent of a TPP by replacing it with an empty consent.
        AisAccountAccessInfoBO access = consent.getAccess();
        if (access == null) {
            return String.format("No account access to tpp with id: %s", consent.getTppId());
        }
        return prepareTemplate(access).toString();
    }

    public String exemptedTemplate() {
        checkNullConsent();
        // Deleting the consent of a TPP by replacing it with an empty consent.
        AisAccountAccessInfoBO access = consent.getAccess();
        if (access == null) {
            return String.format("No account access to tpp with id: %s", consent.getTppId());
        }

        return template() + "This access has been granted. No TAN entry needed.";
    }

    private StringBuilder prepareTemplate(AisAccountAccessInfoBO access) {
        var builder = new StringBuilder(String.format("Account access for TPP with id %s:%n", consent.getTppId()));
        if (consent.getFrequencyPerDay() <= 1) {
            if (consent.isRecurringIndicator()) {
                builder.append("- Up to 1 access per day.\n");
            } else {
                builder.append("- For one time access.\n");
            }
        } else {
            builder.append(String.format("- Up to %s accesses per day.%n", consent.getFrequencyPerDay()));
        }
        if (consent.getValidUntil() != null) {
            builder.append(String.format("- Access valid until %s.%n", formatter.format(consent.getValidUntil())));
        }
        builder.append("Access to following accounts:\n");
        if (ALL_ACCOUNTS.equals(access.getAllPsd2())) {
            builder.append("All payments accounts without balances.\n");
        } else if (ALL_ACCOUNTS_WITH_BALANCES.equals(access.getAllPsd2())) {
            builder.append("All payments accounts with balances and transactions.\n");
        }
        if (ALL_ACCOUNTS.equals(access.getAvailableAccounts())) {
            builder.append("All available accounts without balances.\n");
        } else if (ALL_ACCOUNTS_WITH_BALANCES.equals(access.getAvailableAccounts())) {
            builder.append("All available accounts with balances and transactions.\n");
        }
        format(builder, access.getAccounts(), "Without balances: %s.\n");
        format(builder, access.getBalances(), "With balances: %s.\n");
        format(builder, access.getTransactions(), "With balances and transactions: %s.\n");
        return builder;
    }

    private void format(StringBuilder b, List<String> list, String template) {
        if (CollectionUtils.isNotEmpty(list)) {
            b.append(String.format(template, String.join(" ", list)));
        }
    }

    private void checkNullConsent() { //TODO Get rid of internal validations! Should be done on request level!
        if (consent == null) {
            throw new IllegalStateException("Not expecting consent to be null.");
        }
        if (StringUtils.isEmpty(consent.getTppId())) {
            throw new IllegalStateException("Not expecting tppId to be null.");
        }
    }
}

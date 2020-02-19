package de.adorsys.ledgers.middleware.api.domain.payment;

import java.util.HashMap;
import java.util.Map;

public enum FrequencyCodeTO {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    EVERYTWOWEEKS("EveryTwoWeeks"),
    MONTHLY("Monthly"),
    EVERYTWOMONTHS("EveryTwoMonths"),
    QUARTERLY("Quarterly"),
    SEMIANNUAL("SemiAnnual"),
    ANNUAL("Annual"),
    MONTHLYVARIABLE("Monthlyvariable");

    private String value;

    private static final Map<String, FrequencyCodeTO> container = new HashMap<>();

    static {
        for (FrequencyCodeTO code : values()) {
            container.put(code.value, code);
        }
    }

    FrequencyCodeTO(String value) {
        this.value = value;
    }

    public static FrequencyCodeTO getByValue(String value) {
        return container.get(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
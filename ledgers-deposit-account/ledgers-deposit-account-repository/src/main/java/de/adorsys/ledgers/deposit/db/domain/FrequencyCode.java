/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The following codes from the \"EventFrequency7Code\" of ISO 20022 are supported. - \"Daily\" - \"Weekly\" - \"EveryTwoWeeks\" - \"Monthly\" - \"EveryTwoMonths\" - \"Quarterly\" - \"SemiAnnual\" - \"Annual\"
 */
public enum FrequencyCode {

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

    FrequencyCode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static FrequencyCode fromValue(String text) {
        for (FrequencyCode b : FrequencyCode.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }


}

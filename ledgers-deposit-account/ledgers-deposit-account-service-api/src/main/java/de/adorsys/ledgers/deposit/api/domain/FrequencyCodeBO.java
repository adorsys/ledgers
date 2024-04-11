/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

/**
 * The following codes from the \"EventFrequency7Code\" of ISO 20022 are supported. - \"Daily\" - \"Weekly\" - \"EveryTwoWeeks\" - \"Monthly\" - \"EveryTwoMonths\" - \"Quarterly\" - \"SemiAnnual\" - \"Annual\"
 */
public enum FrequencyCodeBO {

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

    FrequencyCodeBO(String value) {
        this.value = value;
    }

    
    public static FrequencyCodeBO fromValue(String text) {
        for (FrequencyCodeBO b : FrequencyCodeBO.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


}

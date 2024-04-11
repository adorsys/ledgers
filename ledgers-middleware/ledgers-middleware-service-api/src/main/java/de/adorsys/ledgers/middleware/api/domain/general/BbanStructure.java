/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.general;

import de.adorsys.ledgers.util.random.RandomUtils;
import lombok.Data;

@Data
public class BbanStructure {
    private String countryPrefix;
    private int length;
    private EntryType entryType;

    public String generateRandomBban() {
        return this.entryType.bban(this.length);
    }

    public enum EntryType {
        N { // Digits (numeric characters 0 to 9 only)

            @Override
            String bban(int length) {
                return RandomUtils.randomString(length, false, true);
            }
        },
        A {  // Upper case letters (alphabetic characters A-Z only)

            @Override
            String bban(int length) {
                return RandomUtils.randomString(length, true, false).toUpperCase();
            }
        },
        C { // upper and lower case alphanumeric characters (A-Z, a-z and 0-9)

            @Override
            String bban(int length) {
                return RandomUtils.randomString(length, true, true).toUpperCase();
            }
        };

        abstract String bban(int length);
    }
}

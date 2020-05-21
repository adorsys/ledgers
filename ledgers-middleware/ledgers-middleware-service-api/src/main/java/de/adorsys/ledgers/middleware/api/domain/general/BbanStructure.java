package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;

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
                return RandomStringUtils.random(length, false, true);
            }
        },
        A {  // Upper case letters (alphabetic characters A-Z only)

            @Override
            String bban(int length) {
                return RandomStringUtils.random(length, true, false).toUpperCase();
            }
        },
        C { // upper and lower case alphanumeric characters (A-Z, a-z and 0-9)

            @Override
            String bban(int length) {
                return RandomStringUtils.random(length, true, true);
            }
        };

        abstract String bban(int length);
    }
}

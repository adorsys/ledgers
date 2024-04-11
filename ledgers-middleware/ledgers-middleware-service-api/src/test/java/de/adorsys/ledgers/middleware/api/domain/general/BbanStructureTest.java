/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.general;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BbanStructureTest {

    @Test
    void generateRandomBban() {
        BbanStructure structure = new BbanStructure();
        structure.setCountryPrefix("DE");
        structure.setLength(10);
        structure.setEntryType(BbanStructure.EntryType.A);
        String regexAllChars = "^([A-Z]{10})";
        check(structure, regexAllChars);

        structure.setEntryType(BbanStructure.EntryType.N);
        String regexAllNumbers = "^([0-9]{10})";
        check(structure, regexAllNumbers);

        structure.setEntryType(BbanStructure.EntryType.C);
        String regexAny = "^([0-9a-zA-Z]{10})";
        check(structure, regexAny);
    }

    private void check(BbanStructure structure, String regex) {
        String result = structure.generateRandomBban();
        assertTrue(isValid(regex, result));
    }

    public static boolean isValid(String regex, String str) {
        return Pattern.compile(regex).matcher(str).find();
    }
}
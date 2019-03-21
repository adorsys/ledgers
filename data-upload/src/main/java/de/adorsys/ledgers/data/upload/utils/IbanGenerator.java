package de.adorsys.ledgers.data.upload.utils;

import java.math.BigInteger;

public class IbanGenerator {
    private static final String BANK_CODE = "76070024";

    public static String generateIban(String tppCode, String bban) {
        if (isDigitsAndSize(tppCode, 8) && isDigitsAndSize(bban, 2)) {
            BigInteger totalNr = new BigInteger(BANK_CODE + tppCode + bban + "131400");
            String checkSum = String.valueOf(98 - totalNr.remainder(BigInteger.valueOf(97)).intValue());
            if (checkSum.length() < 2) {
                checkSum = "0" + checkSum;
            }
            return "DE" + checkSum + BANK_CODE + tppCode + bban;
        }
        throw new IllegalArgumentException(String.format("Inappropriate data for IBAN creation %s %s", tppCode, bban));
    }

    private static boolean isDigitsAndSize(String toCheck, int size) {
        String regex = "\\d+";
        return toCheck.matches(regex) && toCheck.length() == size;
    }
}

package de.adorsys.ledgers.util.random;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    private static final Random random = new SecureRandom();

    private RandomUtils() {
    }

    public static String randomString(int length, boolean letters, boolean numbers) {
        return RandomStringUtils.random(length, 0, 0, letters, numbers, null, random); //NOSONAR
    }

    public static long threadRandomLong(long origin, long bound) {
        return ThreadLocalRandom.current().nextLong(origin, bound); //NOSONAR
    }
}

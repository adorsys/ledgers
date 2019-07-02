package de.adorsys.ledgers.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class Ids {

    @SuppressWarnings("PMD")
    public static String id() {
        UUID uuid = UUID.randomUUID();
        return base64UrlEncodeWithoutPadding(uuid.getMostSignificantBits()) +
                       base64UrlEncodeWithoutPadding(uuid.getLeastSignificantBits());
    }

    private static String base64UrlEncodeWithoutPadding(long l) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(longToBytes(l));
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}

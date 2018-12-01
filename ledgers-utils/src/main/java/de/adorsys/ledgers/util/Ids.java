package de.adorsys.ledgers.util;

import java.util.UUID;

public class Ids {
//	private static final Encoder URL_ENCODER = Base64.getUrlEncoder();
	private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

	@SuppressWarnings("PMD")
	public static String id() {
		return id_compressed();
	}
	
	private static String id_compressed() {
		UUID uuid = UUID.randomUUID();
		return getCompressedUuid(true, uuid);
	}


	/**
	 * Generates a UUID and compresses it into a base 64 character string; this
	 * results in a 22 character string and since each character represents 6 bits
	 * of data that means the result can represent up to 132 bits. However, since a
	 * UUID is only 128 bits, 4 additional randomize bits are inserted into the
	 * result (if desired); this means that the number of available unique IDs is
	 * increased by a factor of 16
	 *
	 * @param enhanced specifies whether or not to enhance the result with 4
	 *                 additional bits of data since a 22 base64 characters can hold
	 *                 132 bits of data and a UUID is only 128 bits
	 * @return a 22 character string where each character is from the file and url
	 *         safe base64 character set [A-Za-z0-9-_]
	 */
	public static String getCompressedUuid(boolean enhanced, UUID uuid) {
		return compressLong(uuid.getMostSignificantBits(), enhanced)
				+ compressLong(uuid.getLeastSignificantBits(), enhanced);
	} // getCompressedUuid()

	// compress a 64 bit number into 11 6-bit characters
	private static String compressLong(long keyIn, boolean enhance) {
		// randomize 2 bits as a prefix for the leftmost character which would
		// otherwise only have 4 bits of data in the 6 bits
		long prefix = enhance ? (long) (Math.random() * 4) << 62 : 0;

		// extract the first 6-bit character from the key
		String result = "" + chars.charAt((int) (keyIn & 0x3f));

		// shifting in 2 extra random bits since we have the room
		long key = ((keyIn >>> 2) | prefix) >>> 4;

		// iterate thru the next 10 characters
		for (int i = 1; i < 11; i++) {
			// strip off the last 6 bits from the key, look up the matching character
			// and prepend that character to the result
			result = chars.charAt((int) (key & 0x3f)) + result;
			// logical bit shift right so we can isolate the next 6 bits
			key = key >>> 6;
		}

		return result;
	} // compressLong()
}

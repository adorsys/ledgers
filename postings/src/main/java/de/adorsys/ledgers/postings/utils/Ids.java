package de.adorsys.ledgers.postings.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class Ids {

	private static Base64 base64 = new Base64();

	public static String id() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return base64.encodeBase64URLSafeString(bb.array());
	}
}

package de.adorsys.ledgers.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.UUID;

public class Ids {
	private static Encoder URL_ENCODER = Base64.getUrlEncoder();
	
	public static String id() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return URL_ENCODER.encodeToString(bb.array());
	}
}

package de.adorsys.ledgers.utils;

import com.google.common.io.BaseEncoding;

public class Base16 {
	public static String encode(byte[] bytes){
		return BaseEncoding.base16().encode(bytes);
	}
}

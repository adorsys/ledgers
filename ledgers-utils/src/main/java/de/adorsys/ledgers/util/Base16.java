/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util;

import com.google.common.io.BaseEncoding;

public class Base16 {
	private Base16() {
	}

	public static String encode(byte[] bytes){
		return BaseEncoding.base16().encode(bytes);
	}
}

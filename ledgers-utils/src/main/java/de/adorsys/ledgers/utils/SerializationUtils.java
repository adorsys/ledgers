package de.adorsys.ledgers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationUtils {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static byte[] writeValueAsBytes(Object value)
	        throws JsonProcessingException{
		return OBJECT_MAPPER.writeValueAsBytes(value);
	}
	public static String writeValueAsString(Object value)
	        throws JsonProcessingException{
		return OBJECT_MAPPER.writeValueAsString(value);
	}
}

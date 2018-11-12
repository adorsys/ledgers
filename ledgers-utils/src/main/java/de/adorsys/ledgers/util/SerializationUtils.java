package de.adorsys.ledgers.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationUtils {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static byte[] writeValueAsBytes(Object value)
	        throws JsonProcessingException{
		return OBJECT_MAPPER.writeValueAsBytes(value);
	}
	public static String writeValueAsString(Object value)
	        throws JsonProcessingException{
		return OBJECT_MAPPER.writeValueAsString(value);
	}
	
	public static <T> T readValueFromString(String src, Class<T> klass)  {
		try {
			return OBJECT_MAPPER.readValue(src, klass);
		} catch (IOException e) {
			throw new IllegalStateException(e); 
		}
	}
}

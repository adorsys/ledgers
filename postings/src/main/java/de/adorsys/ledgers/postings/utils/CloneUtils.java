package de.adorsys.ledgers.postings.utils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CloneUtils {
	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();
	}
	
	public static <T> T cloneObject(T in, Class<T> type){
		if(in==null) return null;
		try {
			byte[]  bytes = objectMapper.writeValueAsBytes(in);
			return objectMapper.readValue(bytes, type);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T> List<T> cloneList(List<T> in, Class<T> type){
		return in.stream().map(t -> cloneObject(t, type)).collect(Collectors.toList());
	}
}

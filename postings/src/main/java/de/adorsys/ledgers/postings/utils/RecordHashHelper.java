package de.adorsys.ledgers.postings.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingTrace;

public class RecordHashHelper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String DEFAULT_HASH_ALG="SHA-256";

	public String computeRecHash(Posting posting) throws NoSuchAlgorithmException, JsonProcessingException {
		if(posting.getRecordAntecedentId()!=null && posting.getRecordAntecedentHash()==null)
			throw new IllegalStateException(String.format("Missing record antecedant hash. Must be set to hash of %s prior saving.", posting.getRecordAntecedentId()));

		// Reset Record hash if not null.
		if(posting.getRecordHash()!=null)
			throw new IllegalStateException("Posting contains a record hash. Set record hash to null before calling this method");
		
		// Check hash algo
		if(posting.getRecordHashAlg()==null)posting.setRecordHashAlg(DEFAULT_HASH_ALG);
		
		// Get string value including hash
		MessageDigest digest = MessageDigest.getInstance(posting.getRecordHashAlg());
		byte[] valueAsBytes = OBJECT_MAPPER.writeValueAsBytes(posting);
		byte[] hash = digest.digest(valueAsBytes);
		return Hex.encodeHexString(hash);
	}

	public String computeRecHash(PostingTrace postingTrace) throws NoSuchAlgorithmException, JsonProcessingException {
		if(postingTrace.getAntTraceId()!=null && postingTrace.getAntTraceHash()==null)
			throw new IllegalStateException(String.format("Missing record antecedant hash. Must be set to hash of %s prior saving.", postingTrace.getAntTraceId()));

		// Reset Record hash if not null.
		if(postingTrace.getHash()!=null)
			throw new IllegalStateException("PostingTrace contains a hash. Set hash to null before calling this method");
		
		// Check hash algo
		if(postingTrace.getHashAlg()==null)postingTrace.setHashAlg(DEFAULT_HASH_ALG);
		
		// Get string value including hash
		MessageDigest digest = MessageDigest.getInstance(postingTrace.getHashAlg());
		byte[] valueAsBytes = OBJECT_MAPPER.writeValueAsBytes(postingTrace);
		byte[] hash = digest.digest(valueAsBytes);
		return Hex.encodeHexString(hash);
	}
}

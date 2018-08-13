package de.adorsys.ledgers.postings.listener;

import java.security.NoSuchAlgorithmException;

import javax.persistence.PrePersist;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.utils.RecordHashHelper;

public class RecordHashListener {

	private static final RecordHashHelper RECORD_HASH_HELPER = new RecordHashHelper();

	@PrePersist
	public void prePersist(Posting posting){
		if(posting==null) return;
		if(posting.getRecordHash()!=null) return;// Just a copy. Skip
		
		// Check record time available.
		if(posting.getRecordTime()==null) throw new IllegalStateException("Missing record time");
		
		posting.setRecordHash(null);
		String recHash;
		try {
			recHash = RECORD_HASH_HELPER.computeRecHash(posting);
		} catch (NoSuchAlgorithmException | JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
		posting.setRecordHash(recHash);
	}
}

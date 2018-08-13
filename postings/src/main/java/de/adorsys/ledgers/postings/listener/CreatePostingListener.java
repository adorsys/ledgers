package de.adorsys.ledgers.postings.listener;

import java.time.LocalDateTime;

import javax.persistence.PrePersist;

import de.adorsys.ledgers.postings.domain.Posting;

public class CreatePostingListener {

	@PrePersist
	public void prePersist(Posting posting){
		if(posting==null) return;
		// Record hash set means nothing shall change aggain.
		if(posting.getRecordHash()!=null) return;// Just a copy. Skip
		posting.setRecordTime(LocalDateTime.now());
	}
}

package de.adorsys.ledgers.postings.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingBO;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class PostingMapper {
    public PostingBO toPostingBO(Posting posting) {
    	return CloneUtils.cloneObject(posting, PostingBO.class);
    }

    public Posting toPosting(PostingBO posting) {
    	return CloneUtils.cloneObject(posting, Posting.class);
    }
}
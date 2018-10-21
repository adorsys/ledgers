package de.adorsys.ledgers.postings.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.domain.PostingLineBO;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class PostingLineMapper {
	
    PostingLineBO toPostingLineBO(PostingLine posting) {
    	return CloneUtils.cloneObject(posting, PostingLineBO.class);
    }

    PostingLine toPostingLine(PostingLineBO posting) {
    	return CloneUtils.cloneObject(posting, PostingLine.class);    	
    }
}

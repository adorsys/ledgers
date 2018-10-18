package de.adorsys.ledgers.postings.converter;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostingMapper {
    PostingBO toPostingBO(Posting posting);
    Posting toPosting(PostingBO posting);
}

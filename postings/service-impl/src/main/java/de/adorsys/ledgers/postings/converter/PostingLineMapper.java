package de.adorsys.ledgers.postings.converter;

import de.adorsys.ledgers.postings.domain.PostingLine;
import de.adorsys.ledgers.postings.domain.PostingLineBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostingLineMapper {
    PostingLineBO toPostingLineBO(PostingLine posting);

    PostingLine toPostingLine(PostingLineBO posting);
}

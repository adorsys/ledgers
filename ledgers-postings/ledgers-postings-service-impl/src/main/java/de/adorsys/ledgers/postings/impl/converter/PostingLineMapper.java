/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.db.domain.OperationDetails;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper( uses = LedgerAccountMapper.class)
public interface PostingLineMapper {

    @Mapping(source = "details.opDetails", target = "details")
    PostingLineBO toPostingLineBO(PostingLine posting);

    PostingLine toPostingLine(PostingLineBO posting);

    default OperationDetails stringToOperationDetails(String opDetails) {
        OperationDetails operationDetails = new OperationDetails();
        operationDetails.setOpDetails(opDetails);
        return operationDetails;
    }
}

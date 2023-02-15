/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import org.mapstruct.Mapper;

@Mapper(uses = PostingMapper.class)
public interface AccountStmtMapper {
    AccountStmtBO toAccountStmtBO(AccountStmt accountStmt);

    AccountStmt toAccountStmt(AccountStmtBO accountStmt);
}

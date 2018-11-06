package de.adorsys.ledgers.postings.impl.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class AccountStmtMapper {
    public AccountStmtBO toAccountStmtBO(AccountStmt AccountStmt) {
    	return CloneUtils.cloneObject(AccountStmt, AccountStmtBO.class);
    }

    public AccountStmt toAccountStmt(AccountStmtBO AccountStmt) {
    	return CloneUtils.cloneObject(AccountStmt, AccountStmt.class);
    }
}
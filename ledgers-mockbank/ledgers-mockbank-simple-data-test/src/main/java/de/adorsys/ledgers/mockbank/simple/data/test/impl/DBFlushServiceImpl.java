package de.adorsys.ledgers.mockbank.simple.data.test.impl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.mockbank.simple.data.test.api.DBFlushService;

@Service
@Transactional
public class DBFlushServiceImpl implements DBFlushService{
	
	private static final Logger logger = LoggerFactory.getLogger(DBFlushServiceImpl.class);
	private static final String RAW_SQL_QUERY_DELIMITER = ";";
	
	private final EntityManager em;
	
	public DBFlushServiceImpl(EntityManager em) {
		this.em = em;
	}
	
	@Override
	public void flushDataBase() {
		logger.debug("DBFlushServiceImpl#flushDataBase - Start");
		
		final StringBuilder qBuilder = new StringBuilder();
		
		queryStatements().forEach(query -> {
			appendQuery(qBuilder, query);
		});
		
		executeDelete(qBuilder.toString());
		
		logger.debug("DBFlushServiceImpl#flushDataBase - Done");
	}

	private List<String> queryStatements() {
		String[] stmts = {
			"DELETE FROM SCHEDULED_PAYMENT_ORDER",
			"DELETE FROM PAYMENT_TARGETS",
			"DELETE FROM PAYMENT_TARGET",
			"DELETE FROM PAYMENT",
			"DELETE FROM DEPOSIT_ACCOUNT",
			"DELETE FROM USERS_ROLES",
			"DELETE FROM SCA_DATA",
			"DELETE FROM ACCOUNT_ACCESSES",
			"DELETE FROM USERS",
			"DELETE FROM POSTING_LINE",
			"DELETE FROM POSTING",
			"DELETE FROM LEDGER_ACCOUNT",
			"DELETE FROM LEDGER",
			"DELETE FROM CHART_OF_ACCOUNT"
		};
		return Arrays.asList(stmts);
	}

	private int executeDelete(String query) {
		return em.createNativeQuery(query).executeUpdate();
	}

	private void appendQuery(StringBuilder qBuilder, String query) {
		qBuilder.append(query);
		appendDelimIfNeeded(qBuilder);
	}

	private void appendDelimIfNeeded(StringBuilder qBuilder) {
		if(!StringUtils.endsWith(qBuilder, RAW_SQL_QUERY_DELIMITER)) {qBuilder.append(RAW_SQL_QUERY_DELIMITER);}
	}

}

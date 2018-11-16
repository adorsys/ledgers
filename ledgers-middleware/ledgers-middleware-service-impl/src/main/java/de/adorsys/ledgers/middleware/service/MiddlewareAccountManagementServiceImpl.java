package de.adorsys.ledgers.middleware.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;

@Service
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareAccountManagementServiceImpl.class);
	
	@Autowired
	private DepositAccountService depositAccountService;
	
	@Autowired
	private AccountDetailsMapper accountDetailsMapper;

	@Override
	public void createDepositAccount(AccountDetailsTO depositAccount) throws AccountNotFoundMiddlewareException {
		try {
			depositAccountService.createDepositAccount(accountDetailsMapper.toDepositAccountBO(depositAccount));
		} catch (DepositAccountNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
		}		
	}

	@Override
	public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException {
		DepositAccountDetailsBO accountDetailsBO;
		try {
			accountDetailsBO = depositAccountService.getDepositAccountById(accountId, time, withBalance);
		} catch (DepositAccountNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
		}
		return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO.getAccount(), accountDetailsBO.getBalances());
	}

	@Override
	public AccountDetailsTO getDepositAccountByIBAN(String iban, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException {
		DepositAccountDetailsBO accountDetailsBO;
		try {
			accountDetailsBO = depositAccountService.getDepositAccountByIBAN(iban, time, withBalance);
		} catch (DepositAccountNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
		}
		return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO.getAccount(), accountDetailsBO.getBalances());
	}
}

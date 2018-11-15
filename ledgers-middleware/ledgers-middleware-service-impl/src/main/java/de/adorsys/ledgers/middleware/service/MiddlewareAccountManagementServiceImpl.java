package de.adorsys.ledgers.middleware.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.DepositAccountMiddlewareMapper;
import de.adorsys.ledgers.middleware.service.domain.account.DepositAccountTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;

@Service
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
	
	@Autowired
	private DepositAccountService depositAccountService;
	
	@Autowired
	private DepositAccountMiddlewareMapper depositAccountMapper;

	@Override
	public void createDepositAccount(DepositAccountTO depositAccount) throws AccountNotFoundMiddlewareException {
		try {
			depositAccountService.createDepositAccount(depositAccountMapper.toDepositAccountBO(depositAccount));
		} catch (DepositAccountNotFoundException e) {
			throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
		}		
	}

	@Override
	public DepositAccountTO getDepositAccountByIBAN(String iban) throws AccountNotFoundMiddlewareException {
		try {
			return depositAccountMapper.toDepositAccountTO(depositAccountService.getDepositAccountByIBAN(iban));
		} catch (DepositAccountNotFoundException e) {
			throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
		}
	}
}

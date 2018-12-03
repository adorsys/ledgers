package de.adorsys.ledgers.middleware.impl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareAccountManagementServiceImpl.class);
    private static final LocalDateTime BASE_TIME = LocalDateTime.MIN; //TODO @fpo why we use minimal possible time value?

    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentConverter paymentConverter;
    private final UserService userService;

    @Autowired
    public MiddlewareAccountManagementServiceImpl(DepositAccountService depositAccountService, AccountDetailsMapper accountDetailsMapper, PaymentConverter paymentConverter, UserService userService) {
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.paymentConverter = paymentConverter;
        this.userService = userService;
    }

	@Override
	public void createDepositAccount(AccountDetailsTO depositAccount)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException{
		createDepositAccount(depositAccount, Collections.emptyList());
	}
	
	@Override
	public void createDepositAccount(AccountDetailsTO depositAccount, List<AccountAccessTO> accountAccesss)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException{
		// TODO: Access Control
        try {
        	Map<String, UserBO> persistBuffer = new HashMap<>();
            DepositAccountBO depositAccountBO = depositAccountService.createDepositAccount(accountDetailsMapper.toDepositAccountBO(depositAccount));
            if(accountAccesss!=null) {
            	for (AccountAccessTO accountAccessTO : accountAccesss) {
            		UserBO user = persistBuffer.get(accountAccessTO.getUser().getId());
            		if(user==null) {
            			user = userService.findById(accountAccessTO.getUser().getId());
            		}
					AccountAccessBO accountAccessBO = new AccountAccessBO(depositAccountBO.getIban(), 
							AccessTypeBO.valueOf(accountAccessTO.getAccessType().name()));
					addAccess(user, accountAccessBO, persistBuffer);
				}
            }
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        } catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
    }

    private void addAccess(final UserBO user, AccountAccessBO accountAccessBO, final Map<String, UserBO> persistBuffer) {
		AccountAccessBO existingAac = user.getAccountAccesses().stream().filter(a -> a.getIban().equals(accountAccessBO.getIban()))
			.findFirst()
			.orElseGet(() -> {
				AccountAccessBO aac = new AccountAccessBO();
				aac.setAccessType(accountAccessBO.getAccessType());
				aac.setIban(accountAccessBO.getIban());
				user.getAccountAccesses().add(aac);
				persistBuffer.put(user.getId(), user);
				return aac;
			});
		if(existingAac.getId()==null && existingAac.getAccessType().equals(accountAccessBO.getAccessType())){
			existingAac.setAccessType(accountAccessBO.getAccessType());
			persistBuffer.put(user.getId(), user);
		}
	}

	@Override
    public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException {
        try {
            DepositAccountDetailsBO accountDetailsBO = depositAccountService.getDepositAccountById(accountId, time, true);
            return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException {
        try {
            DepositAccountDetailsBO depositAccountBO = depositAccountService.getDepositAccountByIban(iban, time, false);
            return accountDetailsMapper.toAccountDetailsTO(depositAccountBO);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException {
        logger.info("Retrieving accounts by user login {}", userLogin);
        try {
            UserBO userBO = userService.findByLogin(userLogin);
            List<AccountAccessBO> accountAccess = userBO.getAccountAccesses();
            logger.info("{} accounts were retrieved", accountAccess.size());

            List<String> ibans = accountAccess.stream()
                                         .filter(a -> a.getAccessType() == AccessTypeBO.OWNER)
                                         .map(AccountAccessBO::getIban)
                                         .collect(Collectors.toList());
            logger.info("{} were accounts were filtered as OWN", ibans.size());

            List<DepositAccountDetailsBO> depositAccounts = depositAccountService.getDepositAccountsByIban(ibans, BASE_TIME, false);
            logger.info("{} deposit accounts were found", depositAccounts.size());

            return depositAccounts.stream()
                           .map(accountDetailsMapper::toAccountDetailsTO)
                           .collect(Collectors.toList());
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new UserNotFoundMiddlewareException(e.getMessage());
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public TransactionTO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundMiddlewareException {
        try {
            TransactionDetailsBO transaction = depositAccountService.getTransactionById(accountId, transactionId);
            return paymentConverter.toTransactionTO(transaction);
        } catch (TransactionNotFoundException e) {
            throw new TransactionNotFoundMiddlewareException(e.getMessage(), e);
        }

    }

    @Override
    public List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws AccountNotFoundMiddlewareException {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTimeFrom = dateFrom == null
                                             ? today.atStartOfDay()
                                             : dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo == null
                                           ? today.atTime(LocalTime.MAX)
                                           : dateTo.atTime(LocalTime.MAX);
        try {
            List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(accountId, dateTimeFrom, dateTimeTo);
            return paymentConverter.toTransactionTOList(transactions);
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public boolean confirmFundsAvailability(FundsConfirmationRequestTO request) throws AccountNotFoundMiddlewareException {
        try {
            FundsConfirmationRequestBO requestBO = accountDetailsMapper.toFundsConfirmationRequestBO(request);
            return depositAccountService.confirmationOfFunds(requestBO);
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

	@Override
	public void createDepositAccount(String accountNumberPrefix, String accountNumberSuffix)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void grantAccessToDepositAccount(AccountAccessTO accountAccess)
			throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void grantDeferedThirdPartyReadAccessToDepositAccount(AccountAccessTO accountAccess, LocalDateTime fromTime,
			LocalDateTime toTime) throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AccountDetailsTO> listOfDepositAccounts() {
		// TODO Auto-generated method stub
		return null;
	}
}

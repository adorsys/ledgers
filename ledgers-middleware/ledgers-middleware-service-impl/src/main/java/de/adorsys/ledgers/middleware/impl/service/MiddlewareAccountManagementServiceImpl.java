package de.adorsys.ledgers.middleware.impl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import de.adorsys.ledgers.deposit.api.exception.DepositAccountUncheckedException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
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
    private final UserMapper userMapper;
    private final AisConsentMapper aisConsentMapper;
    
    @Autowired
    private AccessTokenTO accessToken;

    @Autowired
    public MiddlewareAccountManagementServiceImpl(DepositAccountService depositAccountService, 
    		AccountDetailsMapper accountDetailsMapper, PaymentConverter paymentConverter, 
    		UserService userService, UserMapper userMapper, AisConsentMapper aisConsentMapper) {
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.paymentConverter = paymentConverter;
        this.userService = userService;
        this.userMapper = userMapper;
        this.aisConsentMapper  = aisConsentMapper;
    }

	@Override
	public void createDepositAccount(AccountDetailsTO depositAccount)
			throws UserNotFoundMiddlewareException {
		createDepositAccount(depositAccount, Collections.emptyList());
	}
	
	@Override
	public void createDepositAccount(AccountDetailsTO depositAccount, List<AccountAccessTO> accountAccesss)
			throws UserNotFoundMiddlewareException{
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
            	persistBuffer.values().forEach(u -> {
            		try {
						userService.updateAccountAccess(u.getLogin(), u.getAccountAccesses());
					} catch (UserNotFoundException e) {
			            logger.error(e.getMessage(), e);
			            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
					}
            	});
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
                                           ? today.atTime(23,59,59,99)
                                           : dateTo.atTime(23,59,59,99);
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
	public void createDepositAccount(String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
		
		String accNbr = accountNumberPrefix+accountNumberSuffix;

		// if the list is not empty, we mus make sure that account belong to the current user.s
		List<DepositAccountBO> accounts = depositAccountService.findByAccountNumberPrefix(accountNumberPrefix);

		validateInput(accounts, accountNumberPrefix, accountNumberSuffix);

		accDetails.setIban(accNbr);
		
		List<AccountAccessTO> accountAccesses = new ArrayList<>();
		// if caller is a customer
		if(accessToken.getRole()==UserRoleTO.CUSTOMER) {
			// then make him owner of the account.
			UserTO userTO = new UserTO();
			userTO.setId(accessToken.getSub());
			userTO.setLogin(accessToken.getActor());
			AccountAccessTO accountAccess = new AccountAccessTO();
			accountAccess.setAccessType(AccessTypeTO.OWNER);
			accountAccess.setIban(accNbr);
			accountAccess.setUser(userTO);
			accountAccesses.add(accountAccess);
		}
		try {
			createDepositAccount(accDetails, accountAccesses);
		} catch (UserNotFoundMiddlewareException e) {
			throw new AccountMiddlewareUncheckedException(String.format("Can not find user with id %s and login", accessToken.getSub(), accessToken.getActor()));
		}
		
	}

	// Validate that
	private void validateInput(List<DepositAccountBO> accounts, String accountNumberPrefix, String accountNumberSuffix) throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
		// This prefix is still free
		if(accounts.isEmpty()) { 
			return;
		}
		
		// XOR The user is the owner of this prefix
		List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();
		
		// EMpty if user is not owner of this prefix.
		if(accountAccesses.isEmpty()) {
			// User can not own any of those accounts.
			throw new AccountWithPrefixGoneMiddlewareException(String.format("Account prefix %s is gone.", accountNumberPrefix));
		}
		
		List<String> ownedAccounts = filterOwnedAccounts(accountAccesses);
		
		// user already has account with this prefix and suffix
		String accNbr = accountNumberPrefix+accountNumberSuffix;
		if(ownedAccounts.contains(accNbr)) {
			throw new AccountWithSuffixExistsMiddlewareException(String.format("Account with suffix %S and prefix %s already exist", accountNumberPrefix, accountNumberSuffix));
		}
		
		// user owns all accounts with this prefix
		accounts.stream().forEach(a -> {
			ownedAccounts.contains(a.getIban());
		});
	}

	private List<String> filterOwnedAccounts(List<AccountAccessTO> accountAccesses) {
		// All iban owned by this user.
		return accountAccesses.stream()
				.filter(a -> AccessTypeBO.OWNER.equals(a.getAccessType()))
				.map(a -> a.getIban()).collect(Collectors.toList());
	}

	@Override
	public void grantAccessToDepositAccount(AccountAccessTO accountAccess)
			throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		UserBO userBo = loadCurrentUser();
		List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();
		
		// Check that current user owns the account.
		List<String> ownedAccounts = filterOwnedAccounts(accountAccesses);
		
		if(!ownedAccounts.contains(accountAccess.getIban())) {
			throw new InsufficientPermissionMiddlewareException(String.format("Current user with id %s and login %s not owner of the target account with iban %s", userBo.getId(), userBo.getLogin(), accountAccess.getIban()));
		}
		
		addAccess(userBo, userMapper.toAccountAccessBO(accountAccess), new HashMap<>());
		
	}

	private UserBO loadCurrentUser() {
		// Load owner
		UserBO userBo;
		try {
			userBo = userService.findById(accessToken.getSub());
		} catch (UserNotFoundException e) {
			throw new DepositAccountUncheckedException(String.format(
					"Can not find user with id %s. But this user is supposed to exist.", accessToken.getSub()), e);
		}
		return userBo;
	}

	@Override
	public String grantAisConsent(AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
		try {
			return userService.grant(aisConsentMapper.toAisConsentBO(aisConsent));
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public List<AccountDetailsTO> listOfDepositAccounts() {
		List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();
		if(accountAccesses==null || accountAccesses.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> ibans = accountAccesses.stream().map(a -> a.getIban()).collect(Collectors.toList());
		List<DepositAccountDetailsBO> depositAccounts;
		try {
			depositAccounts = depositAccountService.getDepositAccountsByIban(ibans, LocalDateTime.now(), true);
		} catch (DepositAccountNotFoundException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
        return depositAccounts.stream()
                .map(accountDetailsMapper::toAccountDetailsTO)
                .collect(Collectors.toList());
	}

	@Override
	public String iban(String id) {
		return depositAccountService.readIbanById(id);
	}
}

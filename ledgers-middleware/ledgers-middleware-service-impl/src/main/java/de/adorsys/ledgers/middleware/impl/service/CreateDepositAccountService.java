package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateDepositAccountService {

    private static final Logger logger = LoggerFactory.getLogger(CreateDepositAccountService.class);

    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final AccessService accessService;

    @SuppressWarnings("PMD.UnusedFormalParameter")
    public CreateDepositAccountService(DepositAccountService depositAccountService, AccountDetailsMapper accountDetailsMapper, PaymentConverter paymentConverter, UserService userService, UserMapper userMapper, AisConsentBOMapper aisConsentMapper, BearerTokenMapper bearerTokenMapper, AccessTokenMapper accessTokenMapper, AccessTokenTO accessToken, SCAOperationService scaOperationService, SCAUtils scaUtils, AccessService accessService, AmountMapper amountMapper) {
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.accessService = accessService;
    }

    public void createDepositAccount(String userId, AccountDetailsTO depositAccount, List<AccountAccessTO> accountAccesses, String branch)
            throws UserNotFoundMiddlewareException {
        try {
            Map<String, UserBO> persistBuffer = new HashMap<>();

            DepositAccountBO depositAccountBO = depositAccountService.createDepositAccountForBranch(
                    accountDetailsMapper.toDepositAccountBO(depositAccount), userId, branch);

            if (accountAccesses != null) {
                accessService.addAccess(accountAccesses, depositAccountBO, persistBuffer);
            }
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage());
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }
}

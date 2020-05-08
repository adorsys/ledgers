package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AdditionalAccountInformationMapper;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AccountIdentifierTypeBO;
import de.adorsys.ledgers.um.api.domain.AdditionalAccountInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException.blockedSupplier;
import static java.lang.String.format;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareUserManagementServiceImpl implements MiddlewareUserManagementService {
    private static final int NANO_TO_SECOND = 1000000000;
    private final UserService userService;
    private final DepositAccountService depositAccountService;
    private final AccessService accessService;
    private final UserMapper userTOMapper = Mappers.getMapper(UserMapper.class);
    private final PageMapper pageMapper;
    private final AdditionalAccountInformationMapper additionalInfoMapper;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public UserTO create(UserTO user) {
        UserBO userBO = userTOMapper.toUserBO(user);
        return userTOMapper.toUserTO(userService.create(userBO));
    }

    @Override
    public UserTO findById(String id) {
        return userTOMapper.toUserTO(userService.findById(id));
    }

    @Override
    public UserTO findByUserLogin(String userLogin) {
        return userTOMapper.toUserTO(userService.findByLogin(userLogin));
    }

    @Override
    public UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList) {
        UserBO userBO = userService.updateScaData(userTOMapper.toScaUserDataListBO(scaDataList), userLogin);
        return userTOMapper.toUserTO(userBO);
    }

    @Override
    public void updateAccountAccess(ScaInfoTO scaInfo, String userId, AccountAccessTO access) {
        DepositAccountDetailsBO account = depositAccountService.getAccountDetailsByIbanAndCurrency(access.getIban(), access.getCurrency(), LocalDateTime.now(), false);
        if (!account.isEnabled()) {
            throw blockedSupplier(PAYMENT_PROCESSING_FAILURE, account.getAccount().getIban(), account.getAccount().isBlocked()).get();
        }
        UserTO branch = findById(scaInfo.getUserId());
        boolean tppHasAccessToAccount = accessService.userHasAccessToAccount(branch, access.getIban());
        if (!tppHasAccessToAccount) {
            log.error("Branch: {} has no access to account: {}", branch.getLogin(), access.getIban());
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(format("Current Branch does have no access to the requested account: %s", access.getIban()))
                          .build();
        }
        UserTO user = findById(userId);
        if (!branch.getBranch().equals(user.getBranch())) {
            log.error("User id: {} with Branch: {} is not from branch: {}", user.getId(), user.getBranch(), branch.getLogin());

            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(format("Requested user: %s is not a part of the branch: %s", user.getLogin(), scaInfo.getUserLogin()))
                          .build();
        }
        accessService.updateAccountAccess(userTOMapper.toUserBO(user), userTOMapper.toAccountAccessBO(access));
    }

    @Override
    public List<UserTO> listUsers(int page, int size) {
        long start = System.nanoTime();
        List<UserTO> users = userTOMapper.toUserTOList(userService.listUsers(page, size));
        log.info("Retrieving: {} users in {} seconds", users.size(), (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        return users;
    }

    @Override
    public CustomPageImpl<UserTO> getUsersByBranchAndRoles(String countryCode, String branchId, String branchLogin, String userLogin, List<UserRoleTO> roles, Boolean blocked, CustomPageableImpl pageable) {
        return pageMapper.toCustomPageImpl(userService.findUsersByMultipleParamsPaged(countryCode, branchId, branchLogin, userLogin, userTOMapper.toUserRoleBO(roles), blocked, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(userTOMapper::toUserTO));
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userService.countUsersByBranch(branch);
    }

    @Override
    public UserTO updateUser(String branchId, UserTO user) {
        String userId = Optional.ofNullable(user.getId()).orElseThrow(() -> MiddlewareModuleException.builder()
                                                                                    .errorCode(REQUEST_VALIDATION_FAILURE)
                                                                                    .devMsg("User id is not present in request!")
                                                                                    .build());
        if (userService.findById(userId).getBranch().equals(branchId)) {
            UserBO userBO = userTOMapper.toUserBO(user);
            return userTOMapper.toUserTO(userService.updateUser(userBO));
        }
        throw MiddlewareModuleException.builder()
                      .errorCode(INSUFFICIENT_PERMISSION)
                      .devMsg("User doesn't belong to your branch!")
                      .build();
    }

    @Override
    public void updatePassword(String userId, String password) {
        userService.updatePassword(userId, password);
    }

    @Override
    public boolean checkMultilevelScaRequired(String login, String iban) {
        if (!multilevelScaEnable) {
            return false;
        }
        UserBO user = userService.findByLogin(login);

        if (!user.hasAccessToAccount(iban)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg("User doesn't have access to the requested account")
                          .build();
        }

        return accessService.resolveScaWeightByDebtorAccount(user.getAccountAccesses(), iban) < 100;
    }

    @Override
    public boolean checkMultilevelScaRequired(String login, List<AccountReferenceTO> references) {
        if (!multilevelScaEnable) {
            return false;
        }
        UserBO user = userService.findByLogin(login);

        if (CollectionUtils.isEmpty(references)) {
            return user.getAccountAccesses().stream()
                           .anyMatch(a -> a.getScaWeight() < 100);
        }
        boolean allMatch = references.stream()
                                   .allMatch(r -> Optional.ofNullable(r.getCurrency())
                                                          .map(c -> user.hasAccessToAccount(r.getIban(), c))
                                                          .orElse(user.hasAccessToAccount(r.getIban())));
        if (!allMatch) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg("User doesn't have access to the requested account")
                          .build();
        }

        return user.getAccountAccesses().stream()
                       .filter(a -> contained(a, references))
                       .anyMatch(a -> a.getScaWeight() < 100);
    }

    @Override
    public List<AdditionalAccountInformationTO> getAdditionalInformation(ScaInfoTO scaInfoHolder, AccountIdentifierTypeTO accountIdentifierType, String accountIdentifier) {
        List<AdditionalAccountInfoBO> info = AccountIdentifierTypeBO.valueOf(accountIdentifierType.name()).getAdditionalAccountInfo(accountIdentifier, userService::findOwnersByIban, userService::findOwnersByAccountId);
        return additionalInfoMapper.toAdditionalAccountInformationTOs(info);
    }

    private boolean contained(AccountAccessBO access, List<AccountReferenceTO> references) {
        return references.stream()
                       .anyMatch(r -> Optional.ofNullable(r.getCurrency())
                                              .map(c -> access.getCurrency().equals(c) && access.getIban().equalsIgnoreCase(r.getIban()))
                                              .orElse(access.getIban().equalsIgnoreCase(r.getIban())));
    }
}

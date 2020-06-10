package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AdditionalAccountInformationMapper;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException.blockedSupplier;
import static java.lang.String.format;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareUserManagementServiceImpl implements MiddlewareUserManagementService {
    private static final int NANO_TO_SECOND = 1000000000;
    private static final ExecutorService FIXED_THREAD_POOL = Executors.newFixedThreadPool(20);
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
        DepositAccountBO account = depositAccountService.getAccountById(access.getAccountId());
        checkAccountIsEnabled(account);
        UserTO initiator = findById(scaInfo.getUserId());
        checkInitiatorIsPermittedToOperateAccount(access, initiator);
        UserTO user = findById(userId);
        checkUserIsNotABranchAndIsSameBranchAsAccount(user, account);
        checkInitiatorIsPermittedToOperateUser(scaInfo, initiator, user);
        AccountAccessBO newAccess = userTOMapper.toAccountAccessBO(access);
        newAccess.updateAccessFields(account.getIban(), account.getCurrency());
        accessService.updateAccountAccess(userTOMapper.toUserBO(user), newAccess);
        if (initiator.getUserRoles().contains(UserRoleTO.SYSTEM) && StringUtils.isNotBlank(user.getBranch())) {
            UserBO branch = userService.findById(user.getBranch());
            accessService.updateAccountAccess(branch, newAccess);
        }
    }

    private void checkUserIsNotABranchAndIsSameBranchAsAccount(UserTO user, DepositAccountBO account) {
        String devMsg = null;
        if (user.getUserRoles().contains(UserRoleTO.STAFF)) {
            devMsg = format("Requested user: %s is a TPP, thus can not possess access on account that does not belong to one of its users!", user.getLogin());
        }
        if (!user.getBranch().equals(account.getBranch())) {
            devMsg = format("Requested user: %s is from different branch than account: %s!", user.getLogin(), account.getIban());
        }
        if (devMsg != null) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(devMsg)
                          .build();
        }
    }

    private void checkInitiatorIsPermittedToOperateUser(ScaInfoTO scaInfo, UserTO initiator, UserTO user) {
        if (initiator.getUserRoles().contains(UserRoleTO.STAFF) && !initiator.getBranch().equals(user.getBranch())) {
            log.error("User id: {} with Branch: {} is not from initiator: {}", user.getId(), user.getBranch(), initiator.getLogin());

            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(format("Requested user: %s is not a part of the initiator: %s", user.getLogin(), scaInfo.getUserLogin()))
                          .build();
        }
    }

    private void checkInitiatorIsPermittedToOperateAccount(AccountAccessTO access, UserTO initiator) {
        if (initiator.getUserRoles().contains(UserRoleTO.STAFF) && !accessService.userHasAccessToAccount(initiator, access.getIban())) {
            log.error("Branch: {} has no access to account: {}", initiator.getLogin(), access.getIban());
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(format("Current Branch does have no access to the requested account: %s", access.getIban()))
                          .build();
        }
    }

    private void checkAccountIsEnabled(DepositAccountBO account) {
        if (!account.isEnabled()) {
            throw blockedSupplier(INSUFFICIENT_PERMISSION, account.getIban(), account.isBlocked()).get();
        }
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
    public CustomPageImpl<UserTO> getUsersByRoles(List<UserRoleTO> roles, CustomPageableImpl pageable) {
        return pageMapper.toCustomPageImpl(userService.getUsersByRoles(userTOMapper.toUserRoleBO(roles), PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(userTOMapper::toUserTO));
    }

    @Override
    public CustomPageImpl<UserExtendedTO> getUsersByBranchAndRolesExtended(String countryCode, String branchId, String branchLogin, String userLogin, List<UserRoleTO> roles, Boolean blocked, CustomPageableImpl pageable) {
        return pageMapper.toCustomPageImpl(userService.findUsersByMultipleParamsPaged(countryCode, branchId, branchLogin, userLogin, userTOMapper.toUserRoleBO(roles), blocked, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(userTOMapper::toUserExtendedTO));
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

    @Override
    public boolean changeStatus(String userId, boolean isSystemBlock) {
        UserBO user = userService.findById(userId);

        if (!user.getUserRoles().contains(UserRoleBO.CUSTOMER)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg("Only customers can be blocked or unblocked.")
                          .build();
        }

        boolean lockStatusToSet = isSystemBlock ? !user.isSystemBlocked() : !user.isBlocked();

        userService.setUserBlockedStatus(userId, isSystemBlock, lockStatusToSet);

        Set<String> depositAccountIdsToChangeStatus = user.getAccountAccesses().stream()
                                                              .map(AccountAccessBO::getAccountId)
                                                              .collect(Collectors.toSet());

        depositAccountService.changeAccountsBlockedStatus(depositAccountIdsToChangeStatus, isSystemBlock, lockStatusToSet);

        return lockStatusToSet;
    }

    @Override
    public void editBasicSelf(String userId, UserTO user) {
        if (!user.getId().equals(userId)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg("You are not allowed to perform operations on different users!")
                          .build();
        }
        UserBO storedUser = userService.findById(userId);
        storedUser.setLogin(user.getLogin());
        storedUser.setEmail(user.getEmail());
        storedUser.setPin(user.getPin());
        userService.updateUser(storedUser);
    }

    @Override
    public void revertDatabase(String userId, LocalDateTime databaseStateDateTime) {
        // First, all users for this branch should be technically blocked.
        long start = System.nanoTime();
        log.info("Started reverting state for {}", userId);

        systemBlockBranch(userId, true);
        log.info("All branch data is LOCKED in {}seconds", (double) (System.nanoTime() - start) / NANO_TO_SECOND);

        depositAccountService.rollBackBranch(userId, databaseStateDateTime);

        systemBlockBranch(userId, false);
        log.info("Reverted data and unlocked branch in {}s", (double) (System.nanoTime() - start) / NANO_TO_SECOND);

    }

    private void systemBlockBranch(String branchId, boolean statusToSet) {
        CompletableFuture.runAsync(() -> userService.setBranchBlockedStatus(branchId, true, statusToSet), FIXED_THREAD_POOL)
                .thenRunAsync(() -> depositAccountService.changeAccountsBlockedStatus(branchId, true, statusToSet));
    }

    private boolean contained(AccountAccessBO access, List<AccountReferenceTO> references) {
        return references.stream()
                       .anyMatch(r -> Optional.ofNullable(r.getCurrency())
                                              .map(c -> access.getCurrency().equals(c) && access.getIban().equalsIgnoreCase(r.getIban()))
                                              .orElse(access.getIban().equalsIgnoreCase(r.getIban())));
    }
}

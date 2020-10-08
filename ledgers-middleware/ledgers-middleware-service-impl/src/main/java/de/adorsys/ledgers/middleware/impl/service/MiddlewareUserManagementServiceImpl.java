package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AdditionalAccountInformationMapper;
import de.adorsys.ledgers.middleware.impl.converter.KeycloakUserMapper;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.*;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;
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
    private final MiddlewareRecoveryService recoveryService;
    private final KeycloakDataService dataService;
    private final KeycloakUserMapper keycloakUserMapper;
    private final KeycloakDataService keycloakDataService;

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;
    @Value("${ledgers.sca.final.weight:100}")
    private int finalWeight;

    @Override
    @Transactional
    public UserTO create(UserTO user) {
        UserBO createdUser = userService.create(userTOMapper.toUserBO(user));
        try {
            KeycloakUser keycloakUser = keycloakUserMapper.toKeycloakUser(createdUser);
            keycloakUser.setPassword(user.getPin());

            dataService.createUser(keycloakUser);
        } catch (Exception e) {
            if (createdUser.getRolesAsString().contains("SYSTEM") && createdUser.getLogin().equals("admin")) {
                log.info("Initial Admin user is already present in IDP");
            } else {
                throw MiddlewareModuleException.builder()
                              .errorCode(INSUFFICIENT_PERMISSION)
                              .devMsg(format("Could not register user at IDP msg: %s", e.getMessage()))
                              .build();
            }
        }
        if (createdUser.getUserRoles().contains(UserRoleBO.STAFF)) {
            RecoveryPointTO point = new RecoveryPointTO(format("Registered %s user", user.getLogin()));
            recoveryService.createRecoveryPoint(createdUser.getBranch(), point);
        }
        return userTOMapper.toUserTO(createdUser);
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
        UserTO user = findById(userId);
        DepositAccountBO account = depositAccountService.getAccountById(access.getAccountId());
        accessService.updateAccountAccessNewAccount(account, userTOMapper.toUserBO(user), access.getScaWeight());
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
    public List<String> getBranchUserLogins(String branchId) {
        return userService.findUserLoginsByBranch(branchId);
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
            dataService.updateUser(keycloakUserMapper.toKeycloakUser(userBO));
            updatePassword(userId, userBO.getPin());
            return userTOMapper.toUserTO(userService.updateUser(userBO));
        }
        throw MiddlewareModuleException.builder() //TODO Think of this stuff should be rewritten
                      .errorCode(INSUFFICIENT_PERMISSION)
                      .devMsg("User doesn't belong to your branch!")
                      .build();
    }

    @Override
    public void updatePassword(String userId, String password) {
        userService.updatePassword(userId, password);
        dataService.resetPassword(userService.findById(userId).getLogin(), password);
    }

    @Override
    public boolean checkMultilevelScaRequired(String login, String iban) {
        if (!multilevelScaEnable) {
            return false;
        }
        return userService.findByLogin(login).resolveScaWeightByIban(iban) < finalWeight;
    }

    @Override
    public boolean checkMultilevelScaRequired(String login, List<AccountReferenceTO> references) {
        if (!multilevelScaEnable) {
            return false;
        }
        UserBO user = userService.findByLogin(login);
        if (CollectionUtils.isEmpty(references)) {
            return user.getAccountAccesses().stream()
                           .anyMatch(a -> a.getScaWeight() < finalWeight);
        }
        List<AccountAccessBO> accountAccessBOS = userTOMapper.toAccountAccessFromReferenceList(references);
        return user.resolveMinimalWeightForReferences(accountAccessBOS) < finalWeight;
    }

    @Override
    public List<AdditionalAccountInformationTO> getAdditionalInformation(ScaInfoTO scaInfoHolder, AccountIdentifierTypeTO accountIdentifierType, String accountIdentifier) {
        List<AdditionalAccountInfoBO> info = AccountIdentifierTypeBO.valueOf(accountIdentifierType.name()).getAdditionalAccountInfo(accountIdentifier, userService::findOwnersByIban, userService::findOwnersByAccountId);
        return additionalInfoMapper.toAdditionalAccountInformationTOs(info);
    }

    @Override
    public boolean changeStatus(String userId, boolean isSystemBlock) {
        UserBO user = userService.findById(userId);
        boolean lockStatusToSet = isSystemBlock ? !user.isSystemBlocked() : !user.isBlocked();
        userService.setUserBlockedStatus(userId, isSystemBlock, lockStatusToSet);
        depositAccountService.changeAccountsBlockedStatus(user.getAccountIds(), isSystemBlock, lockStatusToSet);
        return lockStatusToSet;
    }

    @Override
    public void editBasicSelf(String userId, UserTO user) {
        UserBO storedUser = userService.findById(userId);
        storedUser.setLogin(user.getLogin());
        storedUser.setEmail(user.getEmail());
        storedUser.setPin(user.getPin());
        userService.updateUser(storedUser);
    }

    @Override
    @SuppressWarnings("PMD:PrematureDeclaration")
    public void revertDatabase(String userId, long recoveryPointId) {
        // First, all users for this branch should be technically blocked.
        long start = System.nanoTime();
        log.info("Started reverting state for {}", userId);
        RecoveryPointTO point = recoveryService.getPointById(userId, recoveryPointId);

        systemBlockBranch(userId, true);
        log.info("All branch data is LOCKED in {} seconds", (double) (System.nanoTime() - start) / NANO_TO_SECOND);

        // Delete data in Keycloak.
        userService.findUsersByBranchAndCreatedAfter(userId, point.getRollBackTime())
                .forEach(user -> keycloakDataService.deleteUser(user.getLogin()));

        // Delete data in Ledgers.
        depositAccountService.rollBackBranch(userId, point.getRollBackTime());
        systemBlockBranch(userId, false);
        log.info("Reverted data and unlocked branch in {}s", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    private void systemBlockBranch(String branchId, boolean statusToSet) {
        CompletableFuture.runAsync(() -> userService.setBranchBlockedStatus(branchId, true, statusToSet), FIXED_THREAD_POOL)
                .thenRunAsync(() -> depositAccountService.changeAccountsBlockedStatus(branchId, true, statusToSet));
    }
}

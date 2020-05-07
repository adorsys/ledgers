/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.*;
import de.adorsys.ledgers.um.db.repository.AisConsentRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.util.tan.encriptor.TanEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.*;
import static de.adorsys.ledgers.util.exception.UserManagementModuleException.getModuleExceptionSupplier;

@Slf4j
@Service
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_WITH_LOGIN_NOT_FOUND = "User with login=%s not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";
    private static final String CONSENT_WITH_ID_S_NOT_FOUND = "Consent with id=%s not found";

    private final UserRepository userRepository;
    private final ScaUserDataService scaUserDataService;
    private final AisConsentRepository consentRepository;
    private final UserConverter userConverter;
    private final PasswordEnc passwordEnc;
    private final TanEncryptor tanEncryptor;
    private final AisConsentMapper aisConsentMapper;

    @Override
    public UserBO create(UserBO user) {
        checkUserAlreadyExists(user);

        checkDuplicateScaMethods(user.getScaUserData());
        UserEntity userEntity = userConverter.toUserPO(user);

        // if user is TPP and has an ID than do not reset it
        if (userEntity.getId() == null) {
            userEntity.setId(Ids.id());
        }

        userEntity.setPin(passwordEnc.encode(userEntity.getId(), user.getPin()));
        hashStaticTan(userEntity);
        UserEntity save = userRepository.save(userEntity);
        return convertToUserBoAndDecodeTan(save);
    }

    @Override
    public List<UserBO> listUsers(int page, int size) {
        List<UserEntity> content = userRepository.findAll(PageRequest.of(page, size)).getContent();
        return userConverter.toUserBOList(content);
    }

    @Override
    public UserBO findById(String id) {
        UserEntity user = userRepository.findById(id)
                                  .orElseThrow(getModuleExceptionSupplier(id, USER_NOT_FOUND, USER_WITH_ID_NOT_FOUND));
        return convertToUserBoAndDecodeTan(user);
    }

    @Override
    public UserBO findByLogin(String login) {
        return userConverter.toUserBO(getUser(login));
    }

    @Override
    public UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(getModuleExceptionSupplier(userLogin, USER_NOT_FOUND, USER_WITH_LOGIN_NOT_FOUND));

        checkDuplicateScaMethods(scaDataList);
        scaUserDataService.ifScaChangedEmailNotValid(userConverter.toUserBO(user).getScaUserData(), scaDataList);
        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.getScaUserData().clear();
        user.getScaUserData().addAll(scaMethods);
        hashStaticTan(user);

        log.info("{} sca methods would be updated", scaMethods.size());
        UserEntity save = userRepository.save(user);
        return convertToUserBoAndDecodeTan(save);
    }

    @Override
    public UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(getModuleExceptionSupplier(userLogin, USER_NOT_FOUND, USER_WITH_LOGIN_NOT_FOUND));

        List<AccountAccess> accountAccesses = userConverter.toAccountAccessListEntity(accountAccessListBO);
        user.getAccountAccesses().clear();
        user.getAccountAccesses().addAll(accountAccesses);

        log.info("{} account accesses would be updated", accountAccesses.size());
        UserEntity save = userRepository.save(user);
        return convertToUserBoAndDecodeTan(save);
    }

    @Override
    public AisConsentBO storeConsent(AisConsentBO consentBO) {
        AisConsentEntity consentEntity = consentRepository.findById(consentBO.getId()).orElse(consentRepository.save(aisConsentMapper.toAisConsentPO(consentBO)));
        return aisConsentMapper.toAisConsentBO(consentEntity);
    }

    @Override
    public AisConsentBO loadConsent(String consentId) {
        AisConsentEntity aisConsentEntity = consentRepository.findById(consentId)
                                                    .orElseThrow(getModuleExceptionSupplier(consentId, CONSENT_NOT_FOUND, CONSENT_WITH_ID_S_NOT_FOUND));
        return aisConsentMapper.toAisConsentBO(aisConsentEntity);
    }

    @Override
    public Page<UserBO> findUsersByMultipleParamsPaged(String countryCode, String branchId, String branchLogin, String userLogin, List<UserRoleBO> roles, Boolean blocked, Pageable pageable) {
        List<Boolean> blockedQueryParam = Optional.ofNullable(blocked)
                                                  .map(Arrays::asList)
                                                  .orElseGet(() -> Arrays.asList(true, false));
        List<String> branchIds = findBranchIdsByMultipleParameters(countryCode, branchId, branchLogin);
        Page<UserBO> users = userRepository.findByBranchInAndLoginContainingAndUserRolesInAndBlockedInAndSystemBlockedFalse(branchIds, userLogin, userConverter.toUserRole(roles), blockedQueryParam, pageable)
                                     .map(userConverter::toUserBO);
        users.forEach(this::decodeStaticTanForUser);
        return users;
    }

    @Override
    public List<String> findBranchIdsByMultipleParameters(String countryCode, String branchId, String branchLogin) {
        return userRepository.findBranchIdsByMultipleParameters(countryCode, branchId, branchLogin, UserRole.STAFF);
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userRepository.countByBranch(branch);
    }

    @Override
    public UserBO updateUser(UserBO userBO) {
        checkDuplicateScaMethods(userBO.getScaUserData());
        checkAndResetValidityScaEmail(userBO);
        UserEntity user = userConverter.toUserPO(userBO);
        checkIfPasswordModifiedAndEncode(user);
        hashStaticTan(user);
        UserEntity save = userRepository.save(user);
        return convertToUserBoAndDecodeTan(save);
    }

    @Override
    public List<UserBO> findUsersByIban(String iban) {
        List<UserBO> users = userConverter.toUserBOList(userRepository.findUsersByIban(iban));
        users.forEach(u -> u.setAccountAccesses(u.getAccountAccesses().stream()
                                                        .filter(accountAccess -> accountAccess.getIban().equals(iban))
                                                        .collect(Collectors.toList())));
        return users;
    }

    @Override
    public List<UserBO> findOwnersByIban(String iban) {
        List<UserBO> users = userConverter.toUserBOList(userRepository.findOwnersByIban(iban, AccessType.OWNER));
        users.forEach(u -> u.setAccountAccesses(u.getAccountAccesses().stream()
                                                        .filter(accountAccess -> accountAccess.getIban().equals(iban))
                                                        .collect(Collectors.toList())));
        return users;
    }

    @Override
    public List<UserBO> findOwnersByAccountId(String accountId) {
        List<UserBO> users = userConverter.toUserBOList(userRepository.findOwnersByAccountId(accountId, AccessType.OWNER));
        users.forEach(u -> u.setAccountAccesses(u.getAccountAccesses().stream()
                                                        .filter(accountAccess -> accountAccess.getAccountId().equals(accountId))
                                                        .collect(Collectors.toList())));
        return users;
    }

    @Override
    public void updatePassword(String userId, String password) {
        UserEntity user = userRepository.findById(userId)
                                  .orElseThrow(getModuleExceptionSupplier(userId, USER_NOT_FOUND, USER_WITH_ID_NOT_FOUND));
        user.setPin(passwordEnc.encode(userId, password));
        userRepository.save(user);
    }

    @Override
    public void setBranchBlockedStatus(String userId, boolean isSystemBlock, boolean statusToSet) {
        if (isSystemBlock) {
            userRepository.updateSystemBlockedStatus(userId, statusToSet);
        } else {
            userRepository.updateBlockedStatus(userId, statusToSet);
        }
    }

    private void checkDuplicateScaMethods(List<ScaUserDataBO> scaUserData) {
        List<ScaUserDataBO> checkedData = Optional.ofNullable(scaUserData).orElse(Collections.emptyList());
        if (new HashSet<>(checkedData).size() != checkedData.size()) {
            throw UserManagementModuleException.builder()
                          .devMsg("Duplicating Sca Methods is forbidden!")
                          .errorCode(DUPLICATE_SCA)
                          .build();
        }
    }

    private void checkIfPasswordModifiedAndEncode(UserEntity user) {
        String oldPin = findById(user.getId()).getPin();
        if (!user.getPin().equals(oldPin)) {
            user.setPin(passwordEnc.encode(user.getId(), user.getPin()));
        }
    }

    private void checkAndResetValidityScaEmail(UserBO user) {
        scaUserDataService.ifScaChangedEmailNotValid(findById(user.getId()).getScaUserData(), user.getScaUserData());
    }

    @NotNull
    public UserEntity getUser(String login) {
        return userRepository.findFirstByLogin(login)
                       .orElseThrow(getModuleExceptionSupplier(login, USER_NOT_FOUND, USER_WITH_LOGIN_NOT_FOUND));
    }

    private UserBO convertToUserBoAndDecodeTan(UserEntity user) {
        UserBO userBO = userConverter.toUserBO(user);
        decodeStaticTanForUser(userBO);
        return userBO;
    }

    private void decodeStaticTanForUser(UserBO user) {
        Optional.ofNullable(user.getScaUserData())
                .ifPresent(d -> d.forEach(this::decodeStaticTan));
    }

    private void decodeStaticTan(ScaUserDataBO scaUserData) {
        if (scaUserData.isUsesStaticTan() && StringUtils.isNotBlank(scaUserData.getStaticTan())) {
            scaUserData.setStaticTan(tanEncryptor.decryptTan(scaUserData.getStaticTan()));
        }
    }

    private void hashStaticTan(UserEntity userEntity) {
        userEntity.getScaUserData().stream()
                .filter(d -> StringUtils.isNotBlank(d.getStaticTan()))
                .forEach(d -> d.setStaticTan(tanEncryptor.encryptTan(d.getStaticTan())));
    }

    private void checkUserAlreadyExists(UserBO userBO) {
        Optional<UserEntity> user = userRepository.findByEmailOrLogin(userBO.getEmail(), userBO.getLogin());
        if (user.isPresent()) {
            String message = String.format("User with this email or login already exists. Email %s. Login %s.",
                                           userBO.getEmail(), userBO.getLogin());
            log.error(message);
            throw UserManagementModuleException.builder()
                          .errorCode(USER_ALREADY_EXISTS)
                          .devMsg(message)
                          .build();
        }
    }
}

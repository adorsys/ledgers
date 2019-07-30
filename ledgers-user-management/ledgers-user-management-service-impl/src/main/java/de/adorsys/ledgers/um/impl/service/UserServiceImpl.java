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
import de.adorsys.ledgers.um.api.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.AisConsentRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.tan.encriptor.TanEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.um.api.exception.UserManagementErrorCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_WITH_LOGIN_NOT_FOUND = "User with login=%s not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";
    private static final String CONSENT_WITH_ID_S_NOT_FOUND = "Consent with id=%s not found";

    private final UserRepository userRepository;
    private final AisConsentRepository consentRepository;
    private final UserConverter userConverter;
    private final PasswordEnc passwordEnc;
    private final TanEncryptor tanEncryptor;
    private final AisConsentMapper aisConsentMapper;

    @Override
    public UserBO create(UserBO user) {
        checkUserAlreadyExists(user);

        UserEntity userEntity = userConverter.toUserPO(user);

        // if user is TPP and has an ID than do not reset it
        if (userEntity.getId() == null) {
            log.info("User with login {} has no id, generating one", userEntity.getLogin());
            userEntity.setId(Ids.id());
        }

        userEntity.setPin(passwordEnc.encode(userEntity.getId(), user.getPin()));
        hashStaticTan(userEntity);

        return userConverter.toUserBO(userRepository.save(userEntity));
    }

    @Override
    public List<UserBO> listUsers(int page, int size) {
        List<UserEntity> content = userRepository.findAll(PageRequest.of(page, size)).getContent();
        return userConverter.toUserBOList(content);
    }

    @Override
    public UserBO findById(String id) {
        UserEntity userPO = userRepository.findById(id)
                                    .orElseThrow(() -> UserManagementModuleException.builder()
                                                               .errorCode(USER_NOT_FOUND)
                                                               .devMsg(String.format(USER_WITH_ID_NOT_FOUND, id))
                                                               .build());
        return userConverter.toUserBO(userPO);
    }

    @Override
    public UserBO findByLogin(String login) {
        return userConverter.toUserBO(getUser(login));
    }

    @Override
    public UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                             .errorCode(USER_NOT_FOUND)
                                                             .devMsg(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin))
                                                             .build());

        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.getScaUserData().clear();
        user.getScaUserData().addAll(scaMethods);
        hashStaticTan(user);

        log.info("{} sca methods would be updated", scaMethods.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                             .errorCode(USER_NOT_FOUND)
                                                             .devMsg(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin))
                                                             .build());

        List<AccountAccess> accountAccesses = userConverter.toAccountAccessListEntity(accountAccessListBO);
        user.getAccountAccesses().clear();
        user.getAccountAccesses().addAll(accountAccesses);

        log.info("{} account accesses would be updated", accountAccesses.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public AisConsentBO storeConsent(AisConsentBO consentBO) {
        AisConsentEntity consentEntity = consentRepository.findById(consentBO.getId()).orElse(consentRepository.save(aisConsentMapper.toAisConsentPO(consentBO)));
        return aisConsentMapper.toAisConsentBO(consentEntity);
    }

    @Override
    public AisConsentBO loadConsent(String consentId) {
        AisConsentEntity aisConsentEntity = consentRepository.findById(consentId)
                                                    .orElseThrow(() -> UserManagementModuleException.builder()
                                                                               .errorCode(CONSENT_NOT_FOUND)
                                                                               .devMsg(String.format(CONSENT_WITH_ID_S_NOT_FOUND, consentId))
                                                                               .build());
        return aisConsentMapper.toAisConsentBO(aisConsentEntity);
    }

    @Override
    public List<UserBO> findByBranchAndUserRolesIn(String branch, List<UserRoleBO> userRoles) {
        List<UserEntity> userEntities = userRepository.findByBranchAndUserRolesIn(branch, userConverter.toUserRole(userRoles));
        return userConverter.toUserBOList(userEntities);
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userRepository.countByBranch(branch);
    }

    private void hashStaticTan(UserEntity userEntity) {
        userEntity.getScaUserData().stream()
                .filter(d -> StringUtils.isNotBlank(d.getStaticTan()))
                .forEach(d -> d.setStaticTan(tanEncryptor.encryptTan(d.getStaticTan())));
    }

    @NotNull
    public UserEntity getUser(String login) {
        return userRepository.findFirstByLogin(login)
                       .orElseThrow(() -> UserManagementModuleException.builder()
                                                  .errorCode(USER_NOT_FOUND)
                                                  .devMsg(String.format(USER_WITH_LOGIN_NOT_FOUND, login))
                                                  .build());
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

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

package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.AccessType;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<UserEntity, String> {

    /**
     * Finds user by its login if exists
     *
     * @param login user login
     * @return user if exists
     */
    Optional<UserEntity> findFirstByLogin(String login);

    /**
     * Finds user by its email or login if exists
     *
     * @param email user email
     * @return user if exists
     */
    Optional<UserEntity> findByEmailOrLogin(String email, String login);

    @Modifying
    @Query("update UserEntity u set u.systemBlocked=?2 where u.branch=?1")
    void updateSystemBlockedStatus(String branchId, boolean status);

    @Modifying
    @Query("update UserEntity u set u.blocked=?2 where u.branch=?1")
    void updateBlockedStatus(String branchId, boolean status);

    @Modifying
    @Query("update UserEntity u set u.systemBlocked=?2 where u.id=?1")
    void updateUserSystemBlockedStatus(String userId, boolean status);

    @Modifying
    @Query("update UserEntity u set u.blocked=?2 where u.id=?1")
    void updateUserBlockedStatus(String userId, boolean status);

    @Query("select distinct u from UserEntity u where u.branch like ?1% and u.branch like  %?2% and u.login like %?3% and ?4 member of u.userRoles and u.systemBlocked=false ")
    List<UserEntity> findBranchIdsByMultipleParameters(String countryCode, String branchId, String branchLogin, UserRole role);

    Page<UserEntity> findByBranchInAndLoginContainingAndUserRolesInAndBlockedInAndSystemBlockedFalse(Collection<String> branch, String login, Collection<UserRole> userRoles, Collection<Boolean> blocked, Pageable pageable);

    Page<UserEntity> findByUserRolesIn(Collection<UserRole> userRoles, Pageable pageable);

    /**
     * Counts amount of users for a branch
     *
     * @param branch branch
     * @return amount of users
     */
    int countByBranch(String branch);

    Optional<UserEntity> findByLoginAndEmail(String login, String email);

    @Query(value = "select distinct u from UserEntity u join u.accountAccesses a where a.iban = ?1")
    List<UserEntity> findUsersByIban(String iban);

    @Query(value = "select distinct u from UserEntity u join u.accountAccesses a where a.iban = ?1 and a.accessType = ?2")
    List<UserEntity> findOwnersByIban(String iban, AccessType accessType);

    @Query(value = "select distinct u from UserEntity u join u.accountAccesses a where a.accountId = ?1 and a.accessType = ?2")
    List<UserEntity> findOwnersByAccountId(String accountId, AccessType accessType);

    List<UserEntity> findByBranch(String branchId);

    List<UserEntity> findByBranchAndCreatedAfter(String branchId, LocalDateTime created);

    @Query(value = "select u from UserEntity u where u.login=?1 or u.email=?1")
    Optional<UserEntity> findByLoginOrEmail(String loginOrEmail);
}

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

import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

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

    /**
     * List all users
     *
     * @return list of users
     */
    @NotNull
    @Override
    List<UserEntity> findAll();

    /**
     * Finds all users of the branch with the given roles
     *
     * @param branch branch
     * @param roles  user roles
     * @return list pf users
     */
    List<UserEntity> findByBranchAndUserRolesIn(String branch, List<UserRole> roles);

    /**
     * Counts amount of users for a branch
     *
     * @param branch branch
     * @return amount of users
     */
    int countByBranch(String branch);

    Optional<UserEntity> findByLoginAndEmail(String login, String email);

    @Query(value = "select distinct u from UserEntity u join u.accountAccesses a where a.iban = ?1")
    List<UserEntity> finUsersByIban(String iban);
}

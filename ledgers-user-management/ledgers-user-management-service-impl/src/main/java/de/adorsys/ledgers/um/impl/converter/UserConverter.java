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

package de.adorsys.ledgers.um.impl.converter;

import java.util.List;

import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.mapstruct.Mapper;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;

@Mapper(componentModel = "spring")
public interface UserConverter {

    UserBO toUserBO(UserEntity user);

    UserEntity toUserPO(UserBO user);

    List<UserBO> toUserBOList(List<UserEntity> user);

    List<UserEntity> toUserEntityList(List<UserBO> user);
 
    ScaUserDataBO toScaUserDataBO(ScaUserDataEntity entity);

    ScaUserDataEntity toScaUserDataEntity(ScaUserDataBO bo);

    List<ScaUserDataBO> toScaUserDataListBO(List<ScaUserDataEntity> list);

    List<ScaUserDataEntity> toScaUserDataListEntity(List<ScaUserDataBO> bos);

    AccountAccessBO toAccountAccessBO(AccountAccess entity);

    AccountAccess toAccountAccessEntity(AccountAccessBO bo);

    List<AccountAccessBO> toAccountAccessListBO(List<AccountAccess> list);

    List<AccountAccess> toAccountAccessListEntity(List<AccountAccessBO> bos);

    List<UserRoleBO> toUserRoleBO(List<UserRole> userRoles);

    List<UserRole> toUserRole(List<UserRoleBO> userRoles);

}

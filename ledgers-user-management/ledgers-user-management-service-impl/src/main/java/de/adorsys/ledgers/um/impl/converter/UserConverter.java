/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface UserConverter {

    UserBO toUserBO(UserEntity user);

    UserExtendedBO toUserExtendedBO(UserEntity source, String branchLogin);

    UserEntity toUserPO(UserBO user);

    List<UserBO> toUserBOList(List<UserEntity> user);

    List<UserEntity> toUserEntityList(List<UserBO> user);

    ScaUserDataBO toScaUserDataBO(ScaUserDataEntity entity);

    @Mapping(target = "id", expression = "java(de.adorsys.ledgers.um.api.domain.ScaUserDataBO.checkId(bo.getId()))")
    ScaUserDataEntity toScaUserDataEntity(ScaUserDataBO bo);

    List<ScaUserDataBO> toScaUserDataListBO(List<ScaUserDataEntity> list);

    List<ScaUserDataEntity> toScaUserDataListEntity(List<ScaUserDataBO> bos);

    AccountAccessBO toAccountAccessBO(AccountAccess entity);

    AccountAccess toAccountAccessEntity(AccountAccessBO bo);

    List<AccountAccessBO> toAccountAccessListBO(List<AccountAccess> list);

    List<AccountAccess> toAccountAccessListEntity(List<AccountAccessBO> bos);

    List<UserRole> toUserRole(List<UserRoleBO> userRoles);
}
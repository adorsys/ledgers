/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserTO toUserTO(UserBO user);

    UserExtendedTO toUserExtendedTO(UserExtendedBO user);

    UserBO toUserBO(UserTO user);

    List<UserTO> toUserTOList(List<UserBO> listUsers);

    ScaUserDataTO toScaUserDataTO(ScaUserDataBO bo);

    List<ScaUserDataBO> toScaUserDataListBO(List<ScaUserDataTO> list);

    List<ScaUserDataTO> toScaUserDataListTO(List<ScaUserDataBO> bos);

    List<UserRoleBO> toUserRoleBO(List<UserRoleTO> userRoles);

    @Mapping(target = "scaWeight", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "accessType", ignore = true)
    @Mapping(target = "weight", ignore = true)
    AccountAccessBO toAccountAccessFromReference(AccountReferenceTO source);

    List<AccountAccessBO> toAccountAccessFromReferenceList(List<AccountReferenceTO> source);
}

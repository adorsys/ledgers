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

package de.adorsys.ledgers.middleware.impl.converter;

import java.util.List;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserTO toUserTO(UserBO user);

    UserBO toUserBO(UserTO user);
    
	List<UserTO> toUserTOList(List<UserBO> listUsers);    

    ScaUserDataBO toScaUserDataBO(ScaUserDataTO userData);

    ScaUserDataTO toScaUserDataTO(ScaUserDataBO bo);

    List<ScaUserDataBO> toScaUserDataListBO(List<ScaUserDataTO> list);

    List<ScaUserDataTO> toScaUserDataListTO(List<ScaUserDataBO> bos);
    
    AccountAccessBO toAccountAccessBO(AccountAccessTO userData);

    AccountAccessTO toAccountAccessTO(AccountAccessBO bo);

    List<AccountAccessBO> toAccountAccessListBO(List<AccountAccessTO> list);

    List<AccountAccessTO> toAccountAccessListTO(List<AccountAccessBO> bos);
    
}

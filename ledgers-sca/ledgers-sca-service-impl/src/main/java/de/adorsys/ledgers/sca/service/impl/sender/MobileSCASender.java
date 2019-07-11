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

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.exception.ScaModuleException;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.sca.exception.SCAErrorCode.SCA_METHOD_NOT_SUPPORTED;

@Service
public class MobileSCASender implements SCASender {
    @Override
    public boolean send(String value, String authCode) {
        throw ScaModuleException.builder()
        .errorCode(SCA_METHOD_NOT_SUPPORTED)
                .devMsg(String.format("Sending SCA via %s not implemented yet","PHONE"))
                .build();
    }

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.MOBILE;
    }
}

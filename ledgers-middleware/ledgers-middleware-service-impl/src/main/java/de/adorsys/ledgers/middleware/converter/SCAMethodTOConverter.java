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

package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SCAMethodTOConverter {

    @Mappings({
            @Mapping(source = "scaMethod", target = "type"),
            @Mapping(source = "methodValue", target = "value")
    })
    SCAMethodTO toSCAMethodTO(ScaUserDataBO bo);

    @InheritInverseConfiguration
    ScaUserDataBO toScaUserDataBO(SCAMethodTO to);

    List<SCAMethodTO> toSCAMethodListTO(List<ScaUserDataBO> list);

    List<ScaUserDataBO> toSCAMethodListBO(List<SCAMethodTO> list);
}

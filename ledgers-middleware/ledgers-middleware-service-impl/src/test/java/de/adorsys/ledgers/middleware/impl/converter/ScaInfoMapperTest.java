/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ScaInfoMapperTest {
    private static final String USER_ID = "werw235asr";
    private static final String AUTH_ID = "234234234kjk";
    private static final String SCA_ID = "2344dfffff";

    private final ScaInfoMapper mapper = Mappers.getMapper(ScaInfoMapper.class);

    @Test
    void toScaInfoBO() {
        // Given
        ScaInfoBO expected = buildScaInfoBO();

        // When
        ScaInfoBO actual = mapper.toScaInfoBO(buildScaInfoTO());

        // Then
        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    void toScaInfoTO() {
        // Given
        ScaInfoTO expected = buildScaInfoTO();

        // When
        ScaInfoTO actual = mapper.toScaInfoTO(buildScaInfoBO());

        // Then
        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    private ScaInfoTO buildScaInfoTO() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTH_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleTO.CUSTOMER);
        return info;
    }

    private ScaInfoBO buildScaInfoBO() {
        ScaInfoBO info = new ScaInfoBO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTH_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleBO.CUSTOMER);
        return info;
    }
}
package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class ScaInfoMapperTest {
    private static final String USER_ID = "werw235asr";
    private static final String AUTH_ID = "234234234kjk";
    private static final String SCA_ID = "2344dfffff";

    private final ScaInfoMapper mapper = Mappers.getMapper(ScaInfoMapper.class);

    @Test
    public void toScaInfoBO() {
        ScaInfoBO expected = buildScaInfoBO();
        ScaInfoBO actual = mapper.toScaInfoBO(buildScaInfoTO());

        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toScaInfoTO() {
        ScaInfoTO expected = buildScaInfoTO();
        ScaInfoTO actual = mapper.toScaInfoTO(buildScaInfoBO());

        assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
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
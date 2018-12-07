package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AuthCodeDataConverterTest {

    @Test
    public void toAuthCodeDataBO() {
        AuthCodeDataConverter mapper = Mappers.getMapper(AuthCodeDataConverter.class);

        AuthCodeDataTO to = readYml(AuthCodeDataTO.class, "auth-code-data.yml");

        AuthCodeDataBO bo = mapper.toAuthCodeDataBO(to);

        assertThat(bo.getOpId(), is(to.getOpId()));
        assertThat(bo.getUserLogin(), is(to.getUserLogin()));
        assertThat(bo.getOpData(), is(to.getOpData()));
        assertThat(bo.getScaUserDataId(), is(to.getScaUserDataId()));
    }

    private <T> T readYml(Class<T> aClass, String file) {
        try {
            return YamlReader.getInstance().getObjectFromInputStream(getClass().getResourceAsStream(file), aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}
package de.adorsys.ledgers.middleware.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public class SCAMethodTOConverterTest {

    private SCAMethodTOConverter mapper = Mappers.getMapper(SCAMethodTOConverter.class);
    private List<ScaUserDataBO> userData;
    private List<SCAMethodTO> scaMethodTOS;

    @Before
    public void setUp() throws Exception {
        userData = getDataFromFile(MiddlewareService.class, "SCAUserDataBO.yml", new TypeReference<List<ScaUserDataBO>>() {});
        scaMethodTOS = getDataFromFile(MiddlewareService.class, "SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {});
    }

    @Test
    public void toSCAMethodTO() {
        SCAMethodTO methodTO = mapper.toSCAMethodTO(userData.get(0));

        assertThat(methodTO, is(scaMethodTOS.get(0)));
    }

    @Test
    public void toSCAMethodListTO() {
        List<SCAMethodTO> methods = mapper.toSCAMethodListTO(userData);

        assertThat(methods,is(scaMethodTOS));
    }

    @Test
    public void toScaUserDataBO() {
        ScaUserDataBO bo = mapper.toScaUserDataBO(scaMethodTOS.get(0));

        assertThat(bo.getScaMethod(), is(userData.get(0).getScaMethod()));
        assertThat(bo.getMethodValue(), is(userData.get(0).getMethodValue()));
    }

    //    todo: replace by javatar-commons version 0.7
    private <T> T getDataFromFile(Class aClass, String fileName, TypeReference<T> typeReference){
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = aClass.getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }
}
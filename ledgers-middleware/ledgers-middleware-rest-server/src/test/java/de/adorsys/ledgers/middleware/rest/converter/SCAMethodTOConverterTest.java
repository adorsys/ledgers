package de.adorsys.ledgers.middleware.rest.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SCAMethodTOConverterTest {

	private static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	private SCAMethodTOConverter mapper = Mappers.getMapper(SCAMethodTOConverter.class);
    private static List<ScaUserDataTO> userData;
    private static List<SCAMethodTO> scaMethodTOS;

    @BeforeClass
    public static void beforeClass() {
        objectMapper.findAndRegisterModules();
        userData = getDataFromFile(SCAMethodTOConverterTest.class, "SCAUserDataBO.yml", new TypeReference<List<ScaUserDataTO>>() {});
        scaMethodTOS = getDataFromFile(SCAMethodTOConverterTest.class, "SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {});
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
    	ScaUserDataTO bo = mapper.toScaUserDataTO(scaMethodTOS.get(0));

        assertThat(bo.getScaMethod(), is(userData.get(0).getScaMethod()));
        assertThat(bo.getMethodValue(), is(userData.get(0).getMethodValue()));
    }

    //    todo: replace by javatar-commons version 0.7
    private static <T> T getDataFromFile(Class aClass, String fileName, TypeReference<T> typeReference){
        InputStream inputStream = aClass.getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }
}

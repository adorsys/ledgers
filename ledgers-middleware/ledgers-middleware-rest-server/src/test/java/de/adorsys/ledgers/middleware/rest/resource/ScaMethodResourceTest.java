package de.adorsys.ledgers.middleware.rest.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.converter.SCAMethodTOConverter;
import de.adorsys.ledgers.middleware.rest.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.rest.resource.ScaMethodResource;

public class ScaMethodResourceTest {
	private static final String USER_LOGIN = "userLogin";

	private MockMvc mockMvc;

	@InjectMocks
	private ScaMethodResource resource;

	@Mock
	private MiddlewareUserManagementService middlewareUserService;

	private static ObjectMapper ymlMapper;
	private static ObjectMapper jsonMapper;

	private static UserTO userTO;
    private static List<SCAMethodTO> scaMethodTOS;

	@BeforeClass
	public static void beforClass() {
		ymlMapper = initMapper(new ObjectMapper(new YAMLFactory()));
		jsonMapper = initMapper(new ObjectMapper());
		try {
			userTO = ymlMapper.readValue(ScaMethodResourceTest.class.getResourceAsStream("user.yml"), UserTO.class);
	        scaMethodTOS = ymlMapper.readValue(ScaMethodResourceTest.class.getResourceAsStream("ScaUserDataTO.yml"), new TypeReference<List<ScaUserDataTO>>() {});
		} catch (IOException e) {
			throw new IllegalStateException("File not found", e);
		}
	}
	
	private static ObjectMapper initMapper(ObjectMapper mapper) {
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		mockMvc = MockMvcBuilders.standaloneSetup(resource).setControllerAdvice(new ExceptionAdvisor())
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
	}

	@Test
	public void getUserScaMethods() throws Exception {

		when(middlewareUserService.findByUserLogin(USER_LOGIN)).thenReturn(userTO);

		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(ScaMethodResource.SCA_METHODS + "/{login}", USER_LOGIN))
				.andDo(print()).andExpect(status().is(HttpStatus.OK.value()))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn();

		String content = mvcResult.getResponse().getContentAsString();
		List<SCAMethodTO> methods = jsonMapper.readValue(content, new TypeReference<List<ScaUserDataTO>>() {});

		assertThat(mvcResult.getResponse().getStatus(), is(200));
		assertThat(scaMethodTOS, is(methods));

		verify(middlewareUserService, times(1)).findByUserLogin(USER_LOGIN);
	}

	@Test
	public void getUserScaMethodsUserNotFound() throws Exception {

		when(middlewareUserService.findByUserLogin(USER_LOGIN)).thenThrow(UserNotFoundMiddlewareException.class);

		mockMvc.perform(MockMvcRequestBuilders.get(ScaMethodResource.SCA_METHODS + "/{login}", USER_LOGIN))
				.andDo(print()).andExpect(status().is(HttpStatus.NOT_FOUND.value()))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn();

		verify(middlewareUserService, times(1)).findByUserLogin(USER_LOGIN);
	}

	@Test
	public void updateUserScaMethods() throws Exception {
		when(middlewareUserService.updateScaData(USER_LOGIN, userTO.getScaUserData())).thenReturn(userTO);

		String stringContent = jsonMapper.writeValueAsString(scaMethodTOS);
		mockMvc.perform(MockMvcRequestBuilders.put(ScaMethodResource.SCA_METHODS + "/{login}", USER_LOGIN)
				.contentType(MediaType.APPLICATION_JSON_UTF8).content(stringContent)).andDo(print())
				.andExpect(status().is(HttpStatus.ACCEPTED.value())).andReturn();

		verify(middlewareUserService, times(1)).updateScaData(USER_LOGIN, userTO.getScaUserData());
	}

	@Test
	public void updateUserScaMethodsUserNotFound() throws Exception {
		when(middlewareUserService.updateScaData(USER_LOGIN, userTO.getScaUserData())).thenThrow(UserNotFoundMiddlewareException.class);

		String stringContent = jsonMapper.writeValueAsString(scaMethodTOS);
		mockMvc.perform(MockMvcRequestBuilders.put(ScaMethodResource.SCA_METHODS + "/{login}", USER_LOGIN)
				.contentType(MediaType.APPLICATION_JSON_UTF8).content(stringContent)).andDo(print())
				.andExpect(status().is(HttpStatus.NOT_FOUND.value())).andReturn();

		verify(middlewareUserService, times(1)).updateScaData(USER_LOGIN, userTO.getScaUserData());
	}
}
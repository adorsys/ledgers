package de.adorsys.ledgers.middleware.rest.mockbank;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;

import de.adorsys.ledgers.middleware.LedgersMiddlewareRestApplication;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
public class UserManagementResourceIT {
	
	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;
	private Gson gson = new Gson();
	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	@Before
	public void before() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		
		UserTO admin = new UserTO();
		admin.setEmail("admin@admin.me");
		admin.setLogin("admin");
		admin.setPin("admin123");
		String payload = gson.toJson(admin);
        this.mockMvc.perform(
        		MockMvcRequestBuilders.post("/management/app/admin")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(payload))
        			.andExpect(MockMvcResultMatchers.status().isOk())
        			.andDo(print())
        			.andExpect(MockMvcResultMatchers.content().string(StringContains.containsString(".")));
	}
	
	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
	    ServletContext servletContext = wac.getServletContext();
	     
	    Assert.assertNotNull(servletContext);
	    Assert.assertTrue(servletContext instanceof MockServletContext);
	    Assert.assertNotNull(wac.getBean("accountResource"));
	}

	@Test
	public void test() {
//		fail("Not yet implemented");
	}

	private MiddlewareTestCaseData loadTestData(String file) {
		InputStream inputStream = UserManagementResourceIT.class.getResourceAsStream(file);
		try {
			return mapper.readValue(inputStream, MiddlewareTestCaseData.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}

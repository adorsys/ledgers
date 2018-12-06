package de.adorsys.ledgers.middleware.rest.mockbank;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

import de.adorsys.ledgers.middleware.LedgersMiddlewareRestApplication;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
public class AppManagementResourceAdminIT {
	
	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	
	@Before
	public void before() throws UserAlreadyExistsMiddlewareException {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
	
	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
	    ServletContext servletContext = wac.getServletContext();
	     
	    Assert.assertNotNull(servletContext);
	    Assert.assertTrue(servletContext instanceof MockServletContext);
	    Assert.assertNotNull(wac.getBean("appManagementResource"));
	}

	@Test
    public void givenPingURI_whenMockMVC_thenReturnsPong() throws Exception {
        this.mockMvc.perform(
        		MockMvcRequestBuilders.get("/management/app/ping"))
        			.andDo(print())
        			.andExpect(MockMvcResultMatchers.content().string("pong"));
    }

	@Test
    public void givenAdminURI_whenMockMVC_thenReturnsAccessToken() throws Exception {
		String payload = AdminPayload.adminPayload();
        this.mockMvc.perform(
        		MockMvcRequestBuilders.post("/management/app/admin")
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(payload))
        			.andExpect(MockMvcResultMatchers.status().isOk())
        			.andDo(print())
        			.andExpect(MockMvcResultMatchers.content().string(StringContains.containsString(".")));
    }
	
	
}

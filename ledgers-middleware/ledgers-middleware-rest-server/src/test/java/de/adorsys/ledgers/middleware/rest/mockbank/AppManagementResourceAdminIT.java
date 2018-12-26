package de.adorsys.ledgers.middleware.rest.mockbank;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.middleware.LedgersMiddlewareRestApplication;
import de.adorsys.ledgers.middleware.rest.resource.AppMgmtResource;

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
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
	DbUnitTestExecutionListener.class })
@DatabaseTearDown(value = { "MiddlewareServiceImplIT-db-delete.xml" }, type = DatabaseOperation.DELETE_ALL)
public class AppManagementResourceAdminIT {
	
	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	
	@Before
	public void before() {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.wac)
				.apply(springSecurity())
				.build();
	}
	
	@Test
	public void givenWac_whenServletContext_thenItProvidesGreetController() {
	    ServletContext servletContext = wac.getServletContext();
	     
	    Assert.assertNotNull(servletContext);
	    Assert.assertTrue(servletContext instanceof MockServletContext);
	    Assert.assertNotNull(wac.getBean(AppMgmtResource.class));
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

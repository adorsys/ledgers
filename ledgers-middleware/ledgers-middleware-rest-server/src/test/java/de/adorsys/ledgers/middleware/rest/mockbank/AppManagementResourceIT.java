/*
 * Copyright (c) 2018-2025 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.mockbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;


/*@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseTearDown(value = {"MiddlewareServiceImplIT-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)*/
class AppManagementResourceIT {
    private ObjectMapper mapper = new ObjectMapper(); //NOPMD

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    private BearerTokenTO bearerToken;

   /* @BeforeEach
    void before() throws Exception {
        this.mockMvc = MockMvcBuilders
                               .webAppContextSetup(this.wac)
                               .apply(springSecurity())
                               .build();
        String payload = mapper.writeValueAsString(AdminPayload.adminPayload());
        MvcResult mvcResult = this.mockMvc.perform(
                MockMvcRequestBuilders.post("/management/app/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                                      .andExpect(MockMvcResultMatchers.status().isOk())
                                      .andExpect(MockMvcResultMatchers.content().string(StringContains.containsString("."))).andReturn();
        String bearerTokenString = mvcResult.getResponse().getContentAsString();
        bearerToken = new ObjectMapper().readValue(bearerTokenString, BearerTokenTO.class);
    }

    @Test*/
    void givenInitURI_whenMockMVC_thenReturnsVoid() throws Exception {  //NOPMD

        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/management/app/init")
                        .header("Authorization", "Bearer " + bearerToken.getAccess_token()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

package de.adorsys.ledgers.mockbank.simple.data.test.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DBFlushResource.class)
public class DBFlushResourceTest {
	
	@Autowired
	MockMvc mockMvc;

    @Test
    public void test_flush_mockbank_db_should_return_200_status() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete(DBFlushResource.FLUSH_PATH);
        mockMvc.perform(builder).andExpect(status().isOk());
    }
}

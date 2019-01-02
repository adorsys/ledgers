package de.adorsys.ledgers.mockbank.simple.data.test.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import de.adorsys.ledgers.mockbank.simple.data.test.api.DataUploadService;

@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DataUploadResource.class)
public class DataUploadResourceTest {
	
	private static final String MOCKBANK_SIMPLE_DATA_TEST_INIT_DATA_YML = "mockbank-simple-data-test-init-data.yml";
	private static final String ORIGINAL_FILE_NAME = "file";
	private byte[] data;
	
	@MockBean
	DataUploadService uploadService;
	
	@Autowired
	MockMvc mockMvc;
	
    @Before
    public void before() throws URISyntaxException, IOException {
       // Load file to post
    	InputStream inputStream = DataUploadResourceTest.class.getResourceAsStream(MOCKBANK_SIMPLE_DATA_TEST_INIT_DATA_YML);
    	this.data = IOUtils.toByteArray(inputStream);
    	Objects.requireNonNull(this.data, "Multipart data should not be null");
    }
    

    @Test
    public void test_upload_mockbank_should_return_200_status() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(ORIGINAL_FILE_NAME, MOCKBANK_SIMPLE_DATA_TEST_INIT_DATA_YML, "multipart/form-data", data);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .multipart(DataUploadResource.UPLOAD_MOCKBANK_DATA).file(mockFile);
        mockMvc.perform(builder).andExpect(status().isOk());
    }
}

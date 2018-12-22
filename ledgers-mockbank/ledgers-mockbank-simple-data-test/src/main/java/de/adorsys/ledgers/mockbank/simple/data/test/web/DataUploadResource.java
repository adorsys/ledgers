package de.adorsys.ledgers.mockbank.simple.data.test.web;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.test.EnableMockBankSimpleDataTest;
import de.adorsys.ledgers.mockbank.simple.data.test.api.DataUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import pro.javatar.commons.reader.YamlReader;

@RestController
@EnableMockBankSimpleDataTest.MockBankSimpleDataTestResource
@Api(tags = "Upload MockBankData File" , description= "Provide web endpoint to upload a mockbanks data. i.e you can use this endpoint to upload a mockbank-simple-init-data.yml")
public class DataUploadResource {
	
	
	public static final String UPLOAD_MOCKBANK_DATA = "/data-test/upload-mockbank-data";
	private final DataUploadService mockBankSimpleDataUploadService;
	
	public DataUploadResource(DataUploadService mockBankSimpleDataUploadService) {
		super();
		this.mockBankSimpleDataUploadService = mockBankSimpleDataUploadService;
	}
	
	@ApiOperation(value="Allow a user to upload mockbank-data.")
	@PostMapping(UPLOAD_MOCKBANK_DATA)
    public void handleFileUpload(@RequestParam("file") MultipartFile file) {
		try {
			MockbankInitData initData = parse(file.getInputStream());
			mockBankSimpleDataUploadService.loadData(initData);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to upload file", e);
		}
    }

	protected MockbankInitData parse(InputStream dataInputStream)
			throws IOException, JsonParseException, JsonMappingException {
		return YamlReader.getInstance().getObjectFromInputStream(dataInputStream, MockbankInitData.class);
	}
}

package de.adorsys.ledgers.mockbank.simple.data.test.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.mockbank.simple.data.test.api.MockBankSimpleDataUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@MiddlewareUserResource
@Api(tags = "Upload MockBankData File" , description= "Provide web endpoint to upload a mockbanks data. i.e you can use this endpoint to upload a mockbank-simple-init-data.yml")
public class MockBankSimpleDataUploadResource {
	
	
	private final MockBankSimpleDataUploadService mockBankSimpleDataUploadService;
	
	public MockBankSimpleDataUploadResource(MockBankSimpleDataUploadService mockBankSimpleDataUploadService) {
		super();
		this.mockBankSimpleDataUploadService = mockBankSimpleDataUploadService;
	}
	
	@ApiOperation(value="Allow a user to upload mockbank-data.")
	@PostMapping("/upload-mockbank-data")
    public void handleFileUpload(@RequestParam("file") MultipartFile file) {
		try {
			mockBankSimpleDataUploadService.loadData(file.getInputStream());
		} catch (Exception e) {
			throw new IllegalStateException("Unable to upload file", e);
		}
    }
}

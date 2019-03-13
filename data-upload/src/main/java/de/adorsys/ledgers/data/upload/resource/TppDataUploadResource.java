package de.adorsys.ledgers.data.upload.resource;

import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.service.ParseService;
import de.adorsys.ledgers.data.upload.service.RestExecutionService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "LDG009 - Data Upload(STAFF access)", description = "Provides access to upload test data for staff members.",
        authorizations = @Authorization(value = "apiKey"))
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Upload was successful"),
        @ApiResponse(code = 403, message = "Access forbidden due to inappropriate user role or token"),
        @ApiResponse(code = 406, message = "Could not parse payload file.")
})
@MiddlewareUserResource
@RestController
@RequestMapping("/staff-access")
public class TppDataUploadResource {
    private static final Logger logger = LoggerFactory.getLogger(TppDataUploadResource.class);
    private final RestExecutionService restExecutionService;
    private final ParseService parseService;

    public TppDataUploadResource(RestExecutionService restExecutionService, ParseService parseService) {
        this.restExecutionService = restExecutionService;
        this.parseService = parseService;
    }

    @ApiOperation(value = "Upload YAML file along with TPP-login and pass", authorizations = @Authorization(value = "apiKey"))
    @PostMapping("/upload")
    public ResponseEntity<String> uploadYamlData(HttpServletRequest request, @RequestBody MultipartFile file) {
        logger.info("Update file received");
        DataPayload parsed = parseService.getDataFromFile(file);
        if (parsed == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Could not parse data");
        }
        logger.info("Read data is successful");
        return restExecutionService.updateLedgers(request.getHeader("Authorization"), parsed)
                       ? ResponseEntity.status(HttpStatus.OK).body("Data successfully updated")
                       : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Could not update data.");
    }
}

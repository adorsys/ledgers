package de.adorsys.ledgers.data.upload.resource;

import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.service.ParseService;
import de.adorsys.ledgers.data.upload.service.RestExecutionService;
import de.adorsys.ledgers.data.upload.service.TestsDataGenerationService;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

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
    private final TestsDataGenerationService generationService;

    public TppDataUploadResource(RestExecutionService restExecutionService, ParseService parseService, TestsDataGenerationService generationService) {
        this.restExecutionService = restExecutionService;
        this.parseService = parseService;
        this.generationService = generationService;
    }

    @ApiOperation(value = "Upload YAML file with basic test data", authorizations = @Authorization(value = "apiKey"))
    @PutMapping("/upload")
    public ResponseEntity<String> uploadYamlData(HttpServletRequest request, @RequestBody MultipartFile file) {
        logger.info("Update file received");
        DataPayload parsed = parseService.getDataFromFile(file);
        if (parsed == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not parse data");
        }
        logger.info("Read data is successful");
        return restExecutionService.updateLedgers(request.getHeader("Authorization"), parsed)
                       ? ResponseEntity.status(HttpStatus.OK).body("Data successfully updated")
                       : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Could not update data.");
    }

    @ApiOperation(value = "Generate YAML test data and upload it to Ledgers", authorizations = @Authorization(value = "apiKey"))
    @GetMapping(value = "/generate")
    public ResponseEntity<Resource> generateFile(HttpServletRequest request) {
        logger.info("request to create test data received");
        try {
            byte[] bytes = generationService.generate(request.getHeader("Authorization"));
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
            HttpHeaders headers = getExportFileHttpHeaders();
            return ResponseEntity.ok()
                           .headers(headers)
                           .contentLength(bytes.length)
                           .contentType(MediaType.APPLICATION_OCTET_STREAM)
                           .body(resource);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error("User could not be found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (FileNotFoundException e) {
            logger.error("Default file template could not be found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private HttpHeaders getExportFileHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return headers;
    }
}

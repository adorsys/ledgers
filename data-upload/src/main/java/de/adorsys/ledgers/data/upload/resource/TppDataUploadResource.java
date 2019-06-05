package de.adorsys.ledgers.data.upload.resource;

import de.adorsys.ledgers.data.upload.model.ServiceResponse;
import de.adorsys.ledgers.data.upload.service.TppDataGenAndUploadService;
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

    private final TppDataGenAndUploadService tppDataService;

    public TppDataUploadResource(TppDataGenAndUploadService tppDataService) {
        this.tppDataService = tppDataService;
    }

    @ApiOperation(value = "Upload YAML file with basic test data", authorizations = @Authorization(value = "apiKey"))
    @PutMapping("/upload")
    public ResponseEntity<String> uploadYamlData(HttpServletRequest request, @RequestBody MultipartFile file) {
        ServiceResponse<Void> response = tppDataService.uploadTestData(file, request.getHeader("Authorization"));
        return response.isSuccess()
                       ? ResponseEntity.status(HttpStatus.OK).body(response.getMessage())
                       : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response.getMessage());
    }

    @ApiOperation(value = "Generate YAML test data and upload it to Ledgers", authorizations = @Authorization(value = "apiKey"))
    @ApiParam(name = "generatePayments", defaultValue = "false")
    @GetMapping(value = "/generate")
    public ResponseEntity<Resource> generateFile(HttpServletRequest request, @RequestParam boolean generatePayments) {
        logger.info("request to create test data received");
        try {
            ServiceResponse<byte[]> response = tppDataService.generate(request.getHeader("Authorization"), generatePayments);
            return createResponseForFileOutput(response.getBody(), response.isSuccess());
        } catch (FileNotFoundException e) {
            logger.error("Default file template could not be found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private ResponseEntity<Resource> createResponseForFileOutput(byte[] payload, boolean isSuccess) {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(payload));
        HttpHeaders headers = getExportFileHttpHeaders();
        ResponseEntity.BodyBuilder builder = isSuccess
                                                     ? ResponseEntity.ok()
                                                     : ResponseEntity.badRequest();
        return builder.headers(headers)
                       .contentLength(payload.length)
                       .contentType(MediaType.APPLICATION_OCTET_STREAM)
                       .body(resource);
    }

    private HttpHeaders getExportFileHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return headers;
    }
}

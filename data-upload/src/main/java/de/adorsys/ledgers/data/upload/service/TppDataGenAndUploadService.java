package de.adorsys.ledgers.data.upload.service;

import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.model.ServiceResponse;
import de.adorsys.ledgers.data.upload.resource.TppDataUploadResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

@Service
public class TppDataGenAndUploadService {
    private static final Logger logger = LoggerFactory.getLogger(TppDataUploadResource.class);

    private final ParseService parseService;
    private final RestExecutionService executionService;
    private final TestsDataGenerationService testsDataGenerationService;

    public TppDataGenAndUploadService(ParseService parseService, RestExecutionService executionService, TestsDataGenerationService testsDataGenerationService) {
        this.parseService = parseService;
        this.executionService = executionService;
        this.testsDataGenerationService = testsDataGenerationService;
    }


    public ServiceResponse<byte[]> generate(String token, boolean generatePayments) throws FileNotFoundException {
        String branch = executionService.getBranchName(token);
        DataPayload dataPayload = parseService.getDefaultData()
                                          .map(d -> testsDataGenerationService.generateData(d, branch, generatePayments))
                                          .orElseThrow(() -> new FileNotFoundException("Seems no data is present in file!"));

        boolean updateLedgers = executionService.updateLedgers(token, dataPayload);
        if (!updateLedgers) {
            logger.error("There was an ERROR updating Ledgers with generated data");
        }
        return new ServiceResponse<>(parseService.getFile(dataPayload));
    }

    public ServiceResponse<Void> uploadTestData(MultipartFile file, String token) {
        logger.info("Update file received");
        DataPayload parsed = parseService.getDataFromFile(file);
        if (parsed == null) {
            return new ServiceResponse<>("Could not parse data");
        }
        logger.info("Read data is successful");
        boolean updateLedgers = executionService.updateLedgers(token, parsed);
        String msg = updateLedgers
                             ? "Data successfully updated"
                             : "Could not update data.";
        logger.info(msg);

        return new ServiceResponse<>(updateLedgers, msg);
    }
}

package de.adorsys.ledgers.data.upload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

@Service
public class ParseService {
    private static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private static final Logger logger = LoggerFactory.getLogger(ParseService.class);

    public DataPayload getDataFromFile(MultipartFile input) {
        try {
            DataPayload payload = objectMapper.readValue(input.getInputStream(), DataPayload.class);
            return checkPayload(payload)
                           ? payload
                           : null;
        } catch (IOException e) {
            logger.error("Could not map file to Object. \n {}", e.getMessage());
            return null;
        }
    }

    private boolean checkPayload(DataPayload payload) {
        return containsNotNullObjs(payload.getAccounts())
                       && containsNotNullObjs(payload.getBalancesList())
                       && containsNotNullObjs(payload.getUsers());
    }

    private boolean containsNotNullObjs(Collection collection) {
        return collection == null || !collection.contains(null);
    }
}

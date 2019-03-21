package de.adorsys.ledgers.data.upload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

@Service
public class ParseService {
    private static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private static final Logger logger = LoggerFactory.getLogger(ParseService.class);
    private static final String DEFAULT_TEMPLATE_YML = "NISP_Testing_Default_Template.yml";

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

    public Optional<DataPayload> getDefaultData() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream resource = classloader.getResourceAsStream(DEFAULT_TEMPLATE_YML);
        try {
            return Optional.of(objectMapper.readValue(resource, DataPayload.class));
        } catch (IOException e) {
            logger.error("Could not readout default NISP file template");
            return Optional.empty();
        }
    }

    public byte[] getFile(DataPayload data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            logger.error("Could not write bytes");
            return new byte[]{};
        }
    }

    private boolean checkPayload(DataPayload payload) {
        return containsNotNullObjs(payload.getAccounts())
                       && containsNotNullObjs(payload.getBalancesList())
                       && containsNotNullObjs(payload.getUsers())
                       && payload.getUsers().stream()
                                  .noneMatch(UserTO::userHasRoles);
    }

    private boolean containsNotNullObjs(Collection collection) {
        return collection == null || !collection.contains(null);
    }
}

package de.adorsys.ledgers.middleware.impl.sca;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class PhotoOtpScaChallengeData extends AbstractScaChallengeData {
    private static final String IMAGE_TAN_PATH = "photo-tan-1.png"; // stub for testing purposes

    @Override
    public ChallengeDataTO getChallengeData(ScaDataInfoTO template) {
        ChallengeDataTO data = super.getChallengeData(template);
        data.setImage(resolveImage());
        return data;
    }

    @Override
    public ScaMethodTypeTO getScaMethodType() {
        return ScaMethodTypeTO.PHOTO_OTP;
    }

    private byte[] resolveImage() {
        try {
            Resource resource = new ClassPathResource(IMAGE_TAN_PATH);
            return IOUtils.toByteArray(resource.getInputStream());
        } catch (IOException e) {
            log.error("Can't read image tan", e);
        }
        return new byte[]{};
    }
}

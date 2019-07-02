package de.adorsys.ledgers.middleware.api.domain.sca;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
public class ChallengeDataTO {
    private byte[] image;
    private String data;
    private String imageLink;
    private Integer otpMaxLength;
    private OtpFormatTO otpFormat;
    private String additionalInformation;

    @JsonIgnore
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(image)
                   && StringUtils.isBlank(data)
                   && StringUtils.isBlank(imageLink)
                   && otpMaxLength == null
                   && otpFormat == null
                   && StringUtils.isBlank(additionalInformation);
    }
}

package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDataTO {
    private byte[] image;
    private String data;
    private String imageLink;
    private Integer otpMaxLength;
    private OtpFormatTO otpFormat;
    private String additionalInformation;
}

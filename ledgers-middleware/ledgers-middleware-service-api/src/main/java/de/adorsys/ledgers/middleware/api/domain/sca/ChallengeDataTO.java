/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDataTO {
    private byte[] image;
    private List<String> data;
    private String imageLink;
    private Integer otpMaxLength;
    private OtpFormatTO otpFormat;
    private String additionalInformation;
}

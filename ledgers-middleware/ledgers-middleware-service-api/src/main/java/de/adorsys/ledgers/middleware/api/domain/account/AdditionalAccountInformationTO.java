package de.adorsys.ledgers.middleware.api.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalAccountInformationTO {
    private String accountOwnerName;
    private Integer scaWeight;
}

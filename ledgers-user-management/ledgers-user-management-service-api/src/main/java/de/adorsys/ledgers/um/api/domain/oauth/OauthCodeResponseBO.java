package de.adorsys.ledgers.um.api.domain.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthCodeResponseBO {
    private String code;
}

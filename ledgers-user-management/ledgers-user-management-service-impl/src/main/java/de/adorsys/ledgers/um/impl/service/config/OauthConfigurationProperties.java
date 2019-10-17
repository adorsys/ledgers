package de.adorsys.ledgers.um.impl.service.config;

import de.adorsys.ledgers.um.api.domain.oauth.GrantTypeBO;
import de.adorsys.ledgers.um.api.domain.oauth.ResponseTypeBO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OauthConfigurationProperties {
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private List<ResponseTypeBO> responseTypesSupported;
    private List<GrantTypeBO> grantTypesSupported;
    private OauthLifeTime lifeTime;

    @Data
    public static class OauthLifeTime {
        private int authCode;
        private int accessToken;
        private int refreshToken;
    }
}

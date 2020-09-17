package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthServerInfoBO;
import de.adorsys.ledgers.um.api.service.OauthAuthorisationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.OauthCodeEntity;
import de.adorsys.ledgers.um.db.repository.OauthCodeRepository;
import de.adorsys.ledgers.um.impl.service.config.OauthConfigurationProperties;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.OAUTH_CODE_INVALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthAuthorisationServiceImpl implements OauthAuthorisationService {
    private final UserService userService;
    private final OauthCodeRepository oauthCodeRepository;
    private final OauthConfigurationProperties oauthConfigProp;

    @Override
    @Transactional
    public OauthCodeResponseBO oauthCode(String userId, String accessToken) {
        UserBO user = userService.findById(userId);
        return resolveOauthCode(user, accessToken);
    }

    private OauthCodeResponseBO resolveOauthCode(UserBO user, String accessToken) {
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(oauthConfigProp.getLifeTime().getAuthCode());

        String code = RandomStringUtils.random(24, true, true);
        Optional<OauthCodeEntity> oauthCodeEntity = oauthCodeRepository.findByUserId(user.getId());

        if (oauthCodeEntity.isPresent()) {
            OauthCodeEntity existed = oauthCodeEntity.get();
            existed.setCode(code);
            existed.setExpiryTime(expiryTime);
            existed.setUsed(false);
            existed.setToken(accessToken);
            return new OauthCodeResponseBO(code);
        }
        OauthCodeEntity saved = oauthCodeRepository.save(new OauthCodeEntity(user.getId(), code, expiryTime, accessToken));
        return new OauthCodeResponseBO(saved.getCode());
    }

    @Override
    @Transactional
    public String oauthToken(String code) {
        OauthCodeEntity oauthCodeEntity = oauthCodeRepository.findByCodeAndUsed(code, false)
                                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                                             .errorCode(OAUTH_CODE_INVALID)
                                                                             .devMsg("Invalid code").build());
        if (oauthCodeEntity.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(OAUTH_CODE_INVALID)
                          .devMsg("Oauth code is expired").build();
        }
        oauthCodeEntity.setUsed(true);
        return oauthCodeEntity.getToken();
    }

    @Override
    public OauthServerInfoBO oauthServerInfo() {
        OauthServerInfoBO info = new OauthServerInfoBO();
        info.setTokenEndpoint(oauthConfigProp.getTokenEndpoint());
        info.setAuthorizationEndpoint(oauthConfigProp.getAuthorizationEndpoint());
        info.setGrantTypesSupported(oauthConfigProp.getGrantTypesSupported());
        info.setResponseTypesSupported(oauthConfigProp.getResponseTypesSupported());
        return info;
    }
}

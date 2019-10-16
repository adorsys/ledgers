package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.TokenUsageBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthTokenResponseBO;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.OauthAuthorisationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.OauthCodeEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.um.db.repository.OauthCodeRepository;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.INVALID_CREDENTIAL;
import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.OAUTH_CODE_INVALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthAuthorisationServiceImpl implements OauthAuthorisationService {
    @Value("${oauth.lifetime.auth_code:2}")
    private int authCodeLifeTime;

    @Value("${oauth.lifetime.access_token:5}")
    private int accessTokenLifeTime;

    private final UserService userService;
    private final PasswordEnc passwordEnc;
    private final OauthCodeRepository oauthCodeRepository;
    private final BearerTokenService bearerTokenService;

    @Override
    @Transactional
    public OauthCodeResponseBO oauthCode(String login, String pin) {
        UserBO user = userService.findByLogin(login);
        boolean success = passwordEnc.verify(user.getId(), pin, user.getPin());
        if (!success) {
            throw UserManagementModuleException.builder()
                          .errorCode(INVALID_CREDENTIAL)
                          .devMsg("Invalid credentials")
                          .build();
        }

        OffsetDateTime expiryTime = OffsetDateTime.now()
                                            .plusMinutes(authCodeLifeTime);

        String code = RandomStringUtils.random(24, true, true);
        Optional<OauthCodeEntity> oauthCodeEntity = oauthCodeRepository.findByUserId(user.getId());

        if (!oauthCodeEntity.isPresent()) {
            OauthCodeEntity saved = oauthCodeRepository.save(new OauthCodeEntity(user.getId(), code, expiryTime));
            return new OauthCodeResponseBO(saved.getCode());
        }
        OauthCodeEntity existed = oauthCodeEntity.get();
        existed.setCode(code);
        existed.setExpiryTime(expiryTime);
        return new OauthCodeResponseBO(code);
    }

    @Override
    public OauthTokenResponseBO oauthToken(String code) {
        OauthCodeEntity oauthCodeEntity = oauthCodeRepository.findByCode(code)
                                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                                             .errorCode(OAUTH_CODE_INVALID)
                                                                             .devMsg("Invalid code").build());
        if (oauthCodeEntity.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(OAUTH_CODE_INVALID)
                          .devMsg("Oauth code is expired").build();
        }
        UserBO user = userService.findById(oauthCodeEntity.getUserId());
        String scaIdParam = Ids.id();

        Date issueTime = new Date();
        Date expires = DateUtils.addMinutes(issueTime, accessTokenLifeTime);

        BearerTokenBO token = bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                                                             null, null, UserRole.CUSTOMER, scaIdParam, scaIdParam, issueTime, expires, TokenUsageBO.LOGIN, null);
        return new OauthTokenResponseBO(token);
    }
}

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeData;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.CAN_NOT_RESOLVE_SCA_CHALLENGE_DATA;

@Service
@RequiredArgsConstructor
public class ScaChallengeDataResolverImpl<T extends ScaChallengeData> implements ScaChallengeDataResolver, InitializingBean {
    private final Map<ScaMethodTypeTO, ScaChallengeData> container = new EnumMap<>(ScaMethodTypeTO.class);
    private final List<T> scaChallengeDataServices;

    @Override
    public void afterPropertiesSet() {
        scaChallengeDataServices.forEach(service -> container.put(service.getScaMethodType(), service));
    }

    @Override
    public ScaChallengeData resolveScaChallengeData(ScaMethodTypeTO scaMethodTypeTO) {
        return Optional.ofNullable(container.get(scaMethodTypeTO))
                       .orElseThrow(() -> MiddlewareModuleException.builder()
                                                  .errorCode(CAN_NOT_RESOLVE_SCA_CHALLENGE_DATA)
                                                  .devMsg("Can't resolve sca challenge data")
                                                  .build());
    }
}

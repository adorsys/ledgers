package de.adorsys.ledgers.middleware.impl.sca;

import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import org.springframework.stereotype.Component;

@Component
public class AppOtpScaChallengeData extends AbstractScaChallengeData {

    @Override
    public ScaMethodTypeTO getScaMethodType() {
        return ScaMethodTypeTO.APP_OTP;
    }
}

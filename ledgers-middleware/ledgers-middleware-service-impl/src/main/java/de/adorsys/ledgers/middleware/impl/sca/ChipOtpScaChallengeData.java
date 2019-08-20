package de.adorsys.ledgers.middleware.impl.sca;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class ChipOtpScaChallengeData implements ScaChallengeData {
    private final Map<String, ChallengeDataTO> challengeDatas;

    @Override
    public ChallengeDataTO getChallengeData(String methodValue) {
        ChallengeDataTO data = challengeDatas.get(getScaMethodType().name());
        data.setAdditionalInformation(String.format(data.getAdditionalInformation(), methodValue));
        return data;
    }

    @Override
    public ScaMethodTypeTO getScaMethodType() {
        return ScaMethodTypeTO.CHIP_OTP;
    }
}

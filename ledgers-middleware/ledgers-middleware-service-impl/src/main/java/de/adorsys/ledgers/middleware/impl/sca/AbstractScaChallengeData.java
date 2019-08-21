package de.adorsys.ledgers.middleware.impl.sca;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
public abstract class AbstractScaChallengeData implements ScaChallengeData {
    @Autowired
    private Map<String, ChallengeDataTO> challengeDatas;

    @Override
    public ChallengeDataTO getChallengeData(ScaDataInfoTO template) {
        ChallengeDataTO data = challengeDatas.get(getScaMethodType().name());
        data.setAdditionalInformation(format(data.getAdditionalInformation(), template.getCode(), template.getScaUserDataTO().getMethodValue()));
        return data;
    }
}

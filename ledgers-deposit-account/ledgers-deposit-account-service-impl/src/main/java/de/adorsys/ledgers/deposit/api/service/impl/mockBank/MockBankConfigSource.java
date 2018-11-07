package de.adorsys.ledgers.deposit.api.service.impl.mockBank;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MockBankConfigSource implements ASPSPConfigSource {
    @Override
    public ASPSPConfigData aspspConfigData() {
        return new ASPSPConfigData();
    }

    @Override
    public List<LedgerAccountModel> chartOfAccount(String coaFile) {
        return Collections.singletonList(new LedgerAccountModel());
    }
}

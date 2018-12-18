package de.adorsys.ledgers.mockbank.simple.data.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import pro.javatar.commons.reader.YamlReader;
public class MockBankConfigSource implements ASPSPConfigSource {

    @Override
    public ASPSPConfigData aspspConfigData() {
        InputStream inputStream = MockBankConfigSource.class.getResourceAsStream("aspsps-data-test-config.yml");
        try {
        	return YamlReader.getInstance().getObjectFromInputStream(inputStream, ASPSPConfigData.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<LedgerAccountModel> chartOfAccount(String coaFile) {
        try {
        	return YamlReader.getInstance().getListFromResource(MockBankConfigSource.class, coaFile, LedgerAccountModel.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}

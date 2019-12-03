package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.util.Ids;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AisConsentMapperTest {

    AisConsentMapper mapper = Mappers.getMapper(AisConsentMapper.class);

    @Test
    public void test() {
        AisConsentBO bo = new AisConsentBO();
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        List<String> list = Arrays.asList("DE80760700240271232400");
        access.setAccounts(list);
        access.setTransactions(list);
        access.setBalances(list);
        bo.setAccess(access);
        bo.setFrequencyPerDay(4);
        bo.setId(Ids.id());
        bo.setRecurringIndicator(true);
        bo.setTppId(Ids.id());
        bo.setUserId(Ids.id());
        bo.setValidUntil(LocalDate.now());

        AisConsentEntity po = mapper.toAisConsentPO(bo);

        Assert.assertEquals(bo.getValidUntil(), po.getValidUntil());
        Assert.assertEquals(bo.getUserId(), po.getUserId());
        Assert.assertEquals(bo.getTppId(), po.getTppId());
        Assert.assertEquals(bo.isRecurringIndicator(), po.isRecurringIndicator());
        Assert.assertEquals(bo.getId(), po.getId());
        Assert.assertEquals(bo.getFrequencyPerDay(), po.getFrequencyPerDay());
        po.getAccounts().containsAll(access.getAccounts());
        po.getTransactions().containsAll(access.getTransactions());
        po.getBalances().containsAll(access.getBalances());

    }

}

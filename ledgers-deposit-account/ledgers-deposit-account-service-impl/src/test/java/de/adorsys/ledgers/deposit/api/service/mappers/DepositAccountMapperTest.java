/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DepositAccountMapperTest {

    private static final LocalDateTime CREATED = LocalDateTime.now();

    @InjectMocks
    private DepositAccountMapperImpl depositAccountMapper;

    @Test
    void toDepositAccountBO() {
        // When
        DepositAccountBO account = depositAccountMapper.toDepositAccountBO(getDepositAccount());

        //Then
        assertEquals(account, getDepositAccountBO());
    }

    @Test
    void toDepositAccount() {
        // When
        DepositAccount account = depositAccountMapper.toDepositAccount(getDepositAccountBO());

        //Then
        assertEquals(account, getDepositAccount());
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                                  "name","displayName", "product", null, AccountType.CASH, "bic",
                                  "Some linked account", AccountUsage.PRIV, "details", false, false, CREATED, BigDecimal.ZERO);
    }

    private DepositAccountBO getDepositAccountBO() {
        DepositAccountBO bo = new DepositAccountBO();
        bo.setId("id");
        bo.setIban("iban");
        bo.setMsisdn("msisdn");
        bo.setCurrency(Currency.getInstance("EUR"));
        bo.setName("name");
        bo.setProduct("product");
        bo.setAccountType(AccountTypeBO.CASH);
        bo.setBic("bic");
        bo.setDisplayName("displayName");
        bo.setLinkedAccounts("Some linked account");
        bo.setUsageType(AccountUsageBO.PRIV);
        bo.setDetails("details");
        bo.setCreated(CREATED);
        bo.setCreditLimit(BigDecimal.ZERO);
        return bo;
    }
}

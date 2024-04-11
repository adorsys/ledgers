/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.repository.AccountStmtRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.PostingLineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountStmtServiceImplTest {
    @InjectMocks
    AccountStmtServiceImpl service;
    @Mock
    private AccountStmtRepository accountStmtRepository;
    @Mock
    private PostingLineRepository postingLineRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    private static final ObjectMapper MAPPER = getObjectMapper();

    private final LedgerAccountBO account = readYml(LedgerAccountBO.class, "LedgerAccount.yml");

    @Test
    void readStmt() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        when(postingLineRepository.findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(any(),any()))
                .thenReturn( List.of(getLine(true, true), getLine(false, true)));
        AccountStmtBO result = service.readStmt(account, LocalDateTime.now());
        assertNotNull(result);
        verify(accountStmtRepository, times(1)).findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(any(), any(), any());
    }

    @Test
    void createStmt() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        when(accountStmtRepository.save(any())).thenReturn(new AccountStmt());
        AccountStmtBO result = service.createStmt(account, LocalDateTime.now());
        assertNotNull(result);
        verify(accountStmtRepository, times(1)).save(any());
    }

    @Test
    void closeStmt() {
        AccountStmtBO result = service.closeStmt(new AccountStmtBO());
        assertNull(result);
    }

    private PostingLine getLine(boolean isDebitLine, boolean isValid) {
        PostingLine line = new PostingLine();
        //line.setAccount(account);
        line.setDebitAmount(isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setCreditAmount(!isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setPstTime(LocalDateTime.now());
        return line;
    }

    private static <T> T readYml(Class<T> aClass, String file) {
        try {
            return MAPPER.readValue(AccountStmtServiceImplTest.class.getResourceAsStream(file), aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
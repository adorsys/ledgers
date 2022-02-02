package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.postings.api.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDetailsMapperTest {
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final LocalDate DATE = LocalDate.now();

    @InjectMocks
    TransactionDetailsMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    private static final ObjectMapper LOCAL_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void toTransactionSigned_payment() throws IOException {
        when(objectMapper.readValue(any(String.class), Matchers.eq(TransactionDetailsBO.class))).thenAnswer(a -> LOCAL_MAPPER.readValue((String) a.getArgument(0), TransactionDetailsBO.class));
        TransactionDetailsBO expected = getExpected(true);
        PostingLineBO given = getPostingLine(true);
        TransactionDetailsBO result = mapper.toTransactionSigned(given);
        assertEquals(expected, result);
    }

    @Test
    void toTransactionSigned_other() throws IOException {
        when(objectMapper.readValue(any(String.class), Matchers.eq(TransactionDetailsBO.class))).thenAnswer(a -> LOCAL_MAPPER.readValue((String) a.getArgument(0), TransactionDetailsBO.class));
        TransactionDetailsBO expected = getExpected(false);
        PostingLineBO given = getPostingLine(false);
        TransactionDetailsBO result = mapper.toTransactionSigned(given);
        assertEquals(expected, result);
    }

    private TransactionDetailsBO getExpected(boolean isPayment) {
        return new TransactionDetailsBO("trId", "entryRef", "endToEndId", "mandateId", "checkId",
                                        "creditorId", DATE, DATE, getAmount(isPayment), null, "CrName", "CrAgent", getAccount(), "ultCreditor",
                                        "debtorName", "debtorAgent", getAccount(), "ultDebtor", "additional info", null, null, PurposeCodeBO.ACCT,
                                        "transactionCode", "proprietaryCode", null);
    }

    private PostingLineBO getPostingLine(boolean isPayment) throws JsonProcessingException {
        PostingLineBO bo = new PostingLineBO();
        bo.setId("id");
        bo.setAccount(getLedgerAccount());
        bo.setDebitAmount(isPayment ? BigDecimal.ONE : BigDecimal.ZERO);
        bo.setCreditAmount(isPayment ? BigDecimal.ZERO : BigDecimal.ONE);
        bo.setDetails(getDetailsString());
        bo.setSrcAccount(null);
        bo.setBaseLine(null);
        bo.setSubOprSrcId("pmtTargetId");
        bo.setRecordTime(DATE_TIME);
        bo.setOprId("oprId");
        bo.setAdditionalInformation("additional info");
        bo.setOprSrc("pmtId");
        bo.setPstTime(DATE_TIME);
        bo.setPstType(PostingTypeBO.BUSI_TX);
        bo.setPstStatus(PostingStatusBO.POSTED);
        bo.setHash(null);
        bo.setDiscardedTime(null);
        return bo;
    }

    private String getDetailsString() throws JsonProcessingException {
        PaymentTargetDetailsBO source = getTarget();
        return LOCAL_MAPPER.writeValueAsString(source);
    }

    private PaymentTargetDetailsBO getTarget() {
        PaymentTargetDetailsBO bo = new PaymentTargetDetailsBO();
        bo.setTransactionId("trId");
        bo.setEntryReference("entryRef");
        bo.setEndToEndId("endToEndId");
        bo.setMandateId("mandateId");
        bo.setCheckId("checkId");
        bo.setCreditorId("creditorId");
        bo.setBookingDate(DATE);
        bo.setValueDate(DATE);
        bo.setTransactionAmount(getAmount(false));
        bo.setExchangeRate(null);
        bo.setAdditionalInformation("additional info");
        bo.setCreditorName("CrName");
        bo.setCreditorAgent("CrAgent");
        bo.setCreditorAccount(getAccount());
        bo.setUltimateCreditor("ultCreditor");
        bo.setDebtorName("debtorName");
        bo.setDebtorAgent("debtorAgent");
        bo.setDebtorAccount(getAccount());
        bo.setUltimateDebtor("ultDebtor");
        bo.setRemittanceInformationStructuredArray(null);
        bo.setRemittanceInformationUnstructuredArray(null);
        bo.setPurposeCode(PurposeCodeBO.ACCT);
        bo.setBankTransactionCode("transactionCode");
        bo.setProprietaryBankTransactionCode("proprietaryCode");
        bo.setBalanceAfterTransaction(null);
        bo.setCreditorAddress(getAddress());
        bo.setPaymentOrderId("paymentId");
        bo.setPaymentType(PaymentTypeBO.SINGLE);
        bo.setPaymentProduct("sepa");
        bo.setTransactionStatus(TransactionStatusBO.ACCC);
        bo.setExchangeRate(null);
        return bo;
    }

    private AddressBO getAddress() {
        AddressBO bo = new AddressBO();
        bo.setCountry("CreditorCountry");
        return bo;
    }

    private AccountReferenceBO getAccount() {
        AccountReferenceBO bo = new AccountReferenceBO();
        bo.setCurrency(Currency.getInstance("EUR"));
        bo.setIban("DebtorIBAN");
        return bo;
    }

    private AmountBO getAmount(boolean isNegative) {
        return new AmountBO(Currency.getInstance("EUR"), isNegative ? BigDecimal.ONE.negate() : BigDecimal.ONE);
    }

    private LedgerAccountBO getLedgerAccount() {
        return new LedgerAccountBO("ledgerName", "ledgerId", DATE_TIME, "userDetails", null, null, new LedgerBO(), new LedgerAccountBO(), new ChartOfAccountBO(), BalanceSideBO.DrCr, AccountCategoryBO.RE);
    }
}
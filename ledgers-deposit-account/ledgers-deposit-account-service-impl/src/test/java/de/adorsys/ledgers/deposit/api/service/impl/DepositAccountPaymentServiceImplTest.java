package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.mappers.CurrencyMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.*;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountPaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String WRONG_PAYMENT_ID = "wrongId";
    private static final PaymentProductBO PAYMENT_PRODUCT = PaymentProductBO.SEPA;
    private static final PaymentTypeBO PAYMENT_TYPE_SINGLE = PaymentTypeBO.SINGLE;
    private static final PaymentTypeBO PAYMENT_TYPE_BULK = PaymentTypeBO.BULK;

    @InjectMocks
    private DepositAccountPaymentServiceImpl paymentService;
    @Mock
    private PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CurrencyMapper currencyMapper;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private PostingService postingService;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void getPaymentStatus() throws PaymentNotFoundException {
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getSinglePayment(Payment.class)));

        PaymentResultBO<TransactionStatusBO> paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getPaymentResult().getName(), is(TransactionStatus.RCVD.getName()));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentStatusWithException() throws PaymentNotFoundException {

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        PaymentResultBO<TransactionStatusBO> paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundException {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(Payment.class), getSinglePayment(PaymentBO.class));
        testGetPaymentById(PAYMENT_ID, getBulkPayment(Payment.class), getBulkPayment(PaymentBO.class));
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentById_not_found() throws PaymentNotFoundException {
        testGetPaymentById(WRONG_PAYMENT_ID, getSinglePayment(Payment.class), getSinglePayment(PaymentBO.class));
        testGetPaymentById(WRONG_PAYMENT_ID, getBulkPayment(Payment.class), getBulkPayment(PaymentBO.class));
    }

    @Test
    public void initiatePayment() {
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment(Payment.class));
        when(paymentRepository.save(any())).thenReturn(getSinglePayment(Payment.class));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePayment(PaymentBO.class));

        PaymentBO result = paymentService.initiatePayment(getSinglePayment(PaymentBO.class));
        assertThat(result).isNotNull();
    }

    @Test
    public void executePayment_Single_Success() throws PaymentNotFoundException, PaymentProcessingException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, DoubleEntryAccountingException, BaseLineException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(getSinglePayment(Payment.class)));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePayment(PaymentBO.class));
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(readFile(LedgerAccountBO.class, "LedgerAccount.yml"));
        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
        when(paymentMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        List<TransactionDetailsBO> result = paymentService.executePayment(PAYMENT_ID, PAYMENT_TYPE_SINGLE, PAYMENT_PRODUCT);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isNotNull();
    }

    @Test
    public void executePayment_Bulk_Batch_false_Success() throws PaymentNotFoundException, PaymentProcessingException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, DoubleEntryAccountingException, BaseLineException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(getBulkPayment(Payment.class)));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getBulkPayment(PaymentBO.class));
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(readFile(LedgerAccountBO.class, "LedgerAccount.yml"));
        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
        when(paymentMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        List<TransactionDetailsBO> result = paymentService.executePayment(PAYMENT_ID, PAYMENT_TYPE_BULK, PAYMENT_PRODUCT);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void executePayment_Bulk_Batch_true_Success() throws PaymentNotFoundException, PaymentProcessingException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, DoubleEntryAccountingException, BaseLineException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(readFile(Payment.class, "PaymentBulkBatchTrue.yml")));
        when(paymentMapper.toPaymentBO(any())).thenReturn(readFile(PaymentBO.class, "PaymentBulkBatchTrue.yml"));
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(readFile(LedgerAccountBO.class, "LedgerAccount.yml"));
        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
        when(paymentMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        List<TransactionDetailsBO> result = paymentService.executePayment(PAYMENT_ID, PAYMENT_TYPE_BULK, PAYMENT_PRODUCT);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
    }

    private <T> void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment) throws PaymentNotFoundException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
        when(paymentRepository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        when(paymentMapper.toPaymentBO(persistedPayment)).thenReturn(expectedPayment);

        PaymentBO result = paymentService.getPaymentById(expectedPayment.getPaymentType(), PAYMENT_PRODUCT, paymentId);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedPayment);
        if (result.getPaymentType() == PaymentTypeBO.BULK) {
            assertThat(result.getTargets().size()).isEqualTo(2);
        }
    }

    private <T> T getSinglePayment(Class<T> t) {
        return readFile(t, "PaymentSingle.yml");
    }

    private <T> T getBulkPayment(Class<T> t) {
        return readFile(t, "PaymentBulk.yml");
    }

    private <T> T readFile(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}
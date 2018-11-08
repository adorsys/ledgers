package de.adorsys.ledgers.deposit.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.PaymentSchedulerService;
import de.adorsys.ledgers.deposit.api.service.mappers.CurrencyMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import pro.javatar.commons.reader.YamlReader;

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
    private TransactionDetailsMapper transactionDetailsMapper = Mappers.getMapper(TransactionDetailsMapper.class);
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
    @Mock
    private PaymentSchedulerService paymentSchedulerService;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void getPaymentStatus() throws PaymentNotFoundException {
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getSinglePayment()));

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatus.RCVD.getName()));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentStatusWithException() throws PaymentNotFoundException {

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundException {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentById_not_found() throws PaymentNotFoundException {
        testGetPaymentById(WRONG_PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(WRONG_PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test
    public void initiatePayment() {
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());

        PaymentBO result = paymentService.initiatePayment(getSinglePaymentBO());
        assertThat(result).isNotNull();
    }

    @Test
    public void executePayment_Single_Success() throws PaymentNotFoundException, PaymentProcessingException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, DoubleEntryAccountingException, BaseLineException {
    	LedgerAccountBO ledgerAccount = readFile(LedgerAccountBO.class, "LedgerAccount.yml");
    	LedgerBO ledger = ledgerAccount.getLedger();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(getSinglePayment()));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
//        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(ledgerAccount);
//        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
//        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(ledger));
//        when(transactionDetailsMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        paymentService.executePayment(PAYMENT_ID);
//        assertThat(result).isNotEmpty();
//        assertThat(result.size()).isEqualTo(1);
//        assertThat(result.get(0)).isNotNull();
    }

    @Test
    public void executePayment_Bulk_Batch_false_Success() throws PaymentNotFoundException, PaymentProcessingException, LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, DoubleEntryAccountingException, BaseLineException {
    	LedgerAccountBO ledgerAccount = readFile(LedgerAccountBO.class, "LedgerAccount.yml");
    	LedgerBO ledger = ledgerAccount.getLedger();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(getBulkPayment()));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getBulkPaymentBO());
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
//        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(ledgerAccount);
//        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
//        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(ledger));
//        when(transactionDetailsMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));

        paymentService.executePayment(PAYMENT_ID);
//        assertThat(result).isNotEmpty();
//        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void executePayment_Bulk_Batch_true_Success() throws LedgerNotFoundException, LedgerAccountNotFoundException, PostingNotFoundException, BaseLineException, DoubleEntryAccountingException, PaymentNotFoundException  {
    	LedgerAccountBO ledgerAccount = readFile(LedgerAccountBO.class, "LedgerAccount.yml");
    	LedgerBO ledger = ledgerAccount.getLedger();
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(getBulkBatchPayment()));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getBulBatchkPaymentBO());
        //TODO uncomment when method is refactored //when(depositAccountConfigService.getLedger()).thenReturn(readFile(LedgerBO.class, "Ledger.yml"));
//        when(ledgerService.findLedgerAccount(any(), anyString())).thenReturn(ledgerAccount);
//        when(postingService.newPosting(any())).thenReturn(readFile(PostingBO.class, "Posting.yml"));
//        when(ledgerService.findLedgerByName(any())).thenReturn(Optional.of(ledger));
//        when(transactionDetailsMapper.toTransaction(any())).thenReturn(readFile(TransactionDetailsBO.class, "Transaction.yml"));
        when(paymentSchedulerService.schedulePaymentExecution(any())).thenReturn(TransactionStatusBO.ACSP);

        paymentService.executePayment(PAYMENT_ID);
//        assertThat(result).isNotEmpty();
//        assertThat(result.size()).isEqualTo(1);
    }

    private <T> void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment) throws PaymentNotFoundException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
        when(paymentRepository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        when(paymentMapper.toPaymentBO(persistedPayment)).thenReturn(expectedPayment);

        PaymentBO result = paymentService.getPaymentById(paymentId);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedPayment);
        if (result.getPaymentType() == PaymentTypeBO.BULK) {
            assertThat(result.getTargets().size()).isEqualTo(2);
        }
    }

    private Payment getSinglePayment() {
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
    }
    private PaymentBO getSinglePaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentSingle.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private Payment getBulkPayment() {
        Payment payment = readFile(Payment.class, "PaymentBulk.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
    }
    private PaymentBO getBulkPaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentBulk.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
    }

    
    private Payment getBulkBatchPayment() {
        Payment payment = readFile(Payment.class, "PaymentBulkBatchTrue.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
    }
    private PaymentBO getBulBatchkPaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentBulkBatchTrue.yml");
        payment.getTargets().stream().forEach(t -> t.setPayment(payment));
        return payment;
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
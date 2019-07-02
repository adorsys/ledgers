package de.adorsys.ledgers.deposit.db.domain;

import lombok.Data;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
//TODO REMOVE unused class
public class ScheduledPaymentOrder {

	/*
	 * Id of the payment object. By enforcing the same
	 * id as the payment order, we make sure each payment
	 * order has at most one scheduler entry. 
	 */
	@Id
	private String paymentOrderId;
	
	/*
	 * Time of execution and posting of the last transaction 
	 * derived from this payment.
	 */
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime lastExecTime;
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime lastPostingTime;
	
	/*
	 * Time for execution and posting for the next transaction.
	 * 
	 */
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime nextExecTime;
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime nextPostingTime;
	/*
	 * This is updated by the thread currently executing the payment. This
	 * can be used as part of the semaphore to prevent parallel execution  of 
	 * the same payment.
	 */
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime execStatusTime;
	
	/*
	 * This is the lease written by the executor. Can be extended by the current
	 * owner of the lock.
	 */
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime leaseExpiration;

	
	/*
	 * Identifies the executing scheduler. This shall be set to null by the executing
	 * schedule when done.
	 */
	private String currentExecutor;
}

package de.adorsys.ledgers.deposit.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

@Entity
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


	public String getPaymentOrderId() {
		return paymentOrderId;
	}


	public void setPaymentOrderId(String paymentOrderId) {
		this.paymentOrderId = paymentOrderId;
	}

	public LocalDateTime getExecStatusTime() {
		return execStatusTime;
	}


	public void setExecStatusTime(LocalDateTime execStatusTime) {
		this.execStatusTime = execStatusTime;
	}


	public LocalDateTime getLeaseExpiration() {
		return leaseExpiration;
	}


	public void setLeaseExpiration(LocalDateTime leaseExpiration) {
		this.leaseExpiration = leaseExpiration;
	}


	public String getCurrentExecutor() {
		return currentExecutor;
	}


	public void setCurrentExecutor(String currentExecutor) {
		this.currentExecutor = currentExecutor;
	}


	public LocalDateTime getLastExecTime() {
		return lastExecTime;
	}


	public void setLastExecTime(LocalDateTime lastExecTime) {
		this.lastExecTime = lastExecTime;
	}


	public LocalDateTime getLastPostingTime() {
		return lastPostingTime;
	}


	public void setLastPostingTime(LocalDateTime lastPostingTime) {
		this.lastPostingTime = lastPostingTime;
	}


	public LocalDateTime getNextExecTime() {
		return nextExecTime;
	}


	public void setNextExecTime(LocalDateTime nextExecTime) {
		this.nextExecTime = nextExecTime;
	}


	public LocalDateTime getNextPostingTime() {
		return nextPostingTime;
	}


	public void setNextPostingTime(LocalDateTime nextPostingTime) {
		this.nextPostingTime = nextPostingTime;
	}

}

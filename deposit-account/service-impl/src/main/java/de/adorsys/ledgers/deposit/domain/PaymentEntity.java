package de.adorsys.ledgers.deposit.domain;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.jetbrains.annotations.NotNull;

@Entity
public class PaymentEntity {
	/*
	 * The is id of the payment request
	 */
	@Id
	private String paymentId;

	/*
	 * If this element equals "true", the PSU prefers only one booking entry. If
	 * this element equals "false", the PSU prefers individual booking of all
	 * contained individual transactions. The ASPSP will follow this preference
	 * according to contracts agreed on with the PSU.
	 * 
	 * This is only used for payments of type de.adorsys.ledgers.deposit.domain.PaymentTypeBO.BULK
	 */
	private Boolean batchBookingPreferred;

	private LocalDate requestedExecutionDate;

	private PaymentTypeBO paymentType;

    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCode frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022
    private byte dayOfExecution; //Day here max 31
	
    @NotNull
    private AccountReference debtorAccount;

    private TransactionStatus transactionStatus;

	@OneToMany
	private List<PaymentTarget> targets;
	
}

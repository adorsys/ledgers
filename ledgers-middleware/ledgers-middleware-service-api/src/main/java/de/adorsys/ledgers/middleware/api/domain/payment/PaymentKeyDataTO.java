package de.adorsys.ledgers.middleware.api.domain.payment;

public class PaymentKeyDataTO {
	public static final String SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your one time transfer order to %s at date %s; account %s; %s %s is: ";
	public static final String PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your recurring transfer order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s is: ";
	public static final String BULK_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your one time bulk transfer order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s is: ";

	public static final String CANCEL_SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for the cancellation of your one time transfer order to %s at date %s; account %s; %s %s is: ";
	public static final String CANCEL_PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for the cancellation of your recurring transfer order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s is: ";
	public static final String CANCEL_BULK_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for for the cancellation of your one time bulk transfer order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s is: ";
	
	private String paymentId;
    private String creditorName;
    private String creditorIban;
    private String amount;
    private String currency;
    
    // Periodic
    private String dayOfExecution;
    private String executionRule;
    private String frequency;
    
    private String paymentType;
    
    // Bulk
    private String paymentsSize;
    
    // Bulk, Future Dated
    private String requestedExecutionDate;
    
    private boolean cancellation;

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getCreditorName() {
		return creditorName;
	}

	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}

	public String getCreditorIban() {
		return creditorIban;
	}

	public void setCreditorIban(String creditorIban) {
		this.creditorIban = creditorIban;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDayOfExecution() {
		return dayOfExecution;
	}

	public void setDayOfExecution(String dayOfExecution) {
		this.dayOfExecution = dayOfExecution;
	}

	public String getExecutionRule() {
		return executionRule;
	}

	public void setExecutionRule(String executionRule) {
		this.executionRule = executionRule;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentsSize() {
		return paymentsSize;
	}

	public void setPaymentsSize(String paymentsSize) {
		this.paymentsSize = paymentsSize;
	}

	public String getRequestedExecutionDate() {
		return requestedExecutionDate;
	}

	public void setRequestedExecutionDate(String requestedExecutionDate) {
		this.requestedExecutionDate = requestedExecutionDate;
	}
	
	
	
	public boolean isCancellation() {
		return cancellation;
	}

	public void setCancellation(boolean cancellation) {
		this.cancellation = cancellation;
	}

	public String template() {
		PaymentTypeTO pt = PaymentTypeTO.valueOf(paymentType);
		switch (pt) {
		case PERIODIC:
			return cancellation
					? cancelPeriodicPaymentMessageTemplate()
							:periodicPaymentMessageTemplate();
		case BULK:
			return cancellation
					? cancelBulkPaymentMessageTemplate()
							:bulkPaymentMessageTemplate();
		default:
			return cancellation
					? cancelSinglePaymentMessageTemplate()
							:singlePaymentMessageTemplate();
		}
	}

	private String singlePaymentMessageTemplate() {
		return String.format(SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String periodicPaymentMessageTemplate() {
		return String.format(PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				creditorName, 
				creditorIban, 
				dayOfExecution,
				executionRule,
				frequency,
				currency, 
				amount)  + "%s";
	}

	private String bulkPaymentMessageTemplate() {
		return String.format(BULK_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelSinglePaymentMessageTemplate() {
		return String.format(CANCEL_SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelPeriodicPaymentMessageTemplate() {
		return String.format(CANCEL_PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				creditorName, 
				creditorIban, 
				dayOfExecution,
				executionRule,
				frequency,
				currency, 
				amount)  + "%s";
	}

	private String cancelBulkPaymentMessageTemplate() {
		return String.format(CANCEL_BULK_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}
}

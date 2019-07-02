package de.adorsys.ledgers.middleware.api.domain.payment;

import lombok.Data;

@Data
public class PaymentCoreDataTO {
	public static final String SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your one time %s order to %s at date %s; account %s; %s %s is: ";
	public static final String PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your recurring %s order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s is: ";
	public static final String BULK_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for your one time bulk %s order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s is: ";

	public static final String SINGLE_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "Your one time %s order to %s at date %s; account %s; %s %s has been received.";
	public static final String PERIODIC_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "Your recurring %s order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s  has been received.";
	public static final String BULK_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "Your one time bulk %s order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s has been received.";
	
	public static final String CANCEL_SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for the cancellation of your one time %s order to %s at date %s; account %s; %s %s is: ";
	public static final String CANCEL_PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for the cancellation of your recurring %s order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s is: ";
	public static final String CANCEL_BULK_PAYMENT_TAN_MESSAGE_TEMPLATE = "The TAN for for the cancellation of your one time bulk %s order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s is: ";

	public static final String CANCEL_SINGLE_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "The cancellation of your one time %s order to %s at date %s; account %s; %s %s has been scheduled.";
	public static final String CANCEL_PERIODIC_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "The cancellation of your recurring %s order to %s; account %s; Day of execution %s; Rule %s, Frequency %s; Amount %s %s has been scheduled.";
	public static final String CANCEL_BULK_PAYMENT_EXEMPTED_MESSAGE_TEMPLATE = "The cancellation of your one time bulk %s order %s reciepient(s) with name(s) %s at date %s; account %s; %s %s has been scheduled.";

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
    
    private String paymentProduct;

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

	public String exemptedTemplate() {
		PaymentTypeTO pt = PaymentTypeTO.valueOf(paymentType);
		switch (pt) {
		case PERIODIC:
			return cancellation
					? cancelPeriodicPaymentExemptedMessageTemplate()
							:periodicPaymentExemptedMessageTemplate();
		case BULK:
			return cancellation
					? cancelBulkPaymentExemptedMessageTemplate()
							:bulkPaymentExemptedMessageTemplate();
		default:
			return cancellation
					? cancelSinglePaymentExemptedMessageTemplate()
							:singlePaymentExemptedMessageTemplate();
		}
	}
	
	private String singlePaymentMessageTemplate() {
		return String.format(SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String periodicPaymentMessageTemplate() {
		return String.format(PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
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
				paymentProduct,
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelSinglePaymentMessageTemplate() {
		return String.format(CANCEL_SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelPeriodicPaymentMessageTemplate() {
		return String.format(CANCEL_PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
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
				paymentProduct,
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

// =======
	
	private String singlePaymentExemptedMessageTemplate() {
		return String.format(SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String periodicPaymentExemptedMessageTemplate() {
		return String.format(PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				creditorIban, 
				dayOfExecution,
				executionRule,
				frequency,
				currency, 
				amount)  + "%s";
	}

	private String bulkPaymentExemptedMessageTemplate() {
		return String.format(BULK_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelSinglePaymentExemptedMessageTemplate() {
		return String.format(CANCEL_SINGLE_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				requestedExecutionDate, 
				creditorIban, 
				currency, 
				amount)  + "%s";
	}

	private String cancelPeriodicPaymentExemptedMessageTemplate() {
		return String.format(CANCEL_PERIODIC_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				creditorName, 
				creditorIban, 
				dayOfExecution,
				executionRule,
				frequency,
				currency, 
				amount)  + "%s";
	}

	private String cancelBulkPaymentExemptedMessageTemplate() {
		return String.format(CANCEL_BULK_PAYMENT_TAN_MESSAGE_TEMPLATE, 
				paymentProduct,
				paymentsSize,
				creditorName, 
				requestedExecutionDate,
				creditorIban, 
				currency, 
				amount)  + "%s";
	}
}

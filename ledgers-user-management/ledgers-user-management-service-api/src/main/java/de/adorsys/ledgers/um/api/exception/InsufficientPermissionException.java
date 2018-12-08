package de.adorsys.ledgers.um.api.exception;

public class InsufficientPermissionException  extends Exception {
	private static final long serialVersionUID = 4997857373998474303L;

	public InsufficientPermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InsufficientPermissionException(String message) {
		super(message);
	}
}

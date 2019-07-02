package de.adorsys.ledgers.um.api.exception;


public class UserScaDataNotFoundException extends Exception {
    private static final String message = "SCA user data with id=%s not found";
    public UserScaDataNotFoundException(String id) {
        super(String.format(message, id));
    }
}
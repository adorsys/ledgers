package de.adorsys.ledgers.data.upload.model;

public class ServiceResponse<T> {
    private boolean success;
    private T body;
    private String message;

    public ServiceResponse(T body) {
        this.success = true;
        this.body = body;
    }

    public ServiceResponse(boolean success, T body) {
        this.success = success;
        this.body = body;
    }

    public ServiceResponse() {
        this.success = true;
    }

    public ServiceResponse(String message) {
        this.success = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public ServiceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public String getMessage() {
        return message;
    }
}

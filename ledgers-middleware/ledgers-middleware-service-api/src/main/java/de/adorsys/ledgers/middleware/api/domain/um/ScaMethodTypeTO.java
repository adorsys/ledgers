package de.adorsys.ledgers.middleware.api.domain.um;

public enum ScaMethodTypeTO {
    SMTP_OTP(false),
    MOBILE(false),
    CHIP_OTP(false),
    PHOTO_OTP(false),
    PUSH_OTP(false),
    SMS_OTP(false),
    APP_OTP(true);

    private final boolean decoupled;

    ScaMethodTypeTO(boolean decoupled) {
        this.decoupled = decoupled;
    }

    public boolean isDecoupled() {
        return decoupled;
    }
}

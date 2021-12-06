package de.adorsys.ledgers.um.db.domain;

public enum ScaMethodType {
    SMTP_OTP(false),
    MOBILE(false),
    CHIP_OTP(false),
    PHOTO_OTP(false),
    PUSH_OTP(false),
    SMS_OTP(false),
    APP_OTP(true);

    private final boolean decoupled;

    ScaMethodType(boolean decoupled) {
        this.decoupled = decoupled;
    }

    public boolean isDecoupled() {
        return decoupled;
    }
}

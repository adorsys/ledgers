package de.adorsys.ledgers.um.db.domain;

public enum ResetPasswordStatus {
    INITIATED,
    SENT,
    VERIFIED,
    FAILED,
    EXPIRED,
    DONE;
}

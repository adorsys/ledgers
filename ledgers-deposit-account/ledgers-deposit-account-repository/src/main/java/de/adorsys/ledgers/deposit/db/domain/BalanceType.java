package de.adorsys.ledgers.deposit.db.domain;

/**
 * Describes the type of balances a deposit account can carry.
 * 
 * @author fpo
 *
 */
public enum BalanceType {
    CLOSING_BOOKED,
    EXPECTED,
    AUTHORISED,
    OPENING_BOOKED,
    INTERIM_AVAILABLE,
    FORWARD_AVAILABLE,
    NONINVOICED
}

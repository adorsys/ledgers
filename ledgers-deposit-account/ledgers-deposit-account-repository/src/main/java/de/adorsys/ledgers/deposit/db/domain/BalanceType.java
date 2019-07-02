package de.adorsys.ledgers.deposit.db.domain;

/**
 * Describes the type of balances a deposit account can carry.
 * 
 * @author fpo
 *
 */
//TODO unused ENUM matter to removal
public enum BalanceType {
    CLOSING_BOOKED,
    EXPECTED,
    AUTHORISED,
    OPENING_BOOKED,
    INTERIM_AVAILABLE,
    FORWARD_AVAILABLE,
    NONINVOICED
}

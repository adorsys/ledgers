package de.adorsys.ledgers.deposit.api.domain;

/**
 * Describes the type of balances a deposit account can carry.
 * 
 * @author fpo
 *
 */
public enum BalanceTypeBO {
    CLOSING_BOOKED,
    EXPECTED,
    AUTHORISED,
    OPENING_BOOKED,
    INTERIM_AVAILABLE,
    FORWARD_AVAILABLE,
    NONINVOICED,
    AVAILABLE
}

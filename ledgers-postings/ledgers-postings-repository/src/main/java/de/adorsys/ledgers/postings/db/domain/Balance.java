package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class Balance {
	private BalanceSide balanceSide;
	private Amount amount;
    private BalanceType balanceType;
    private LocalDateTime lastChangeDateTime;
    private LocalDate referenceDate;
    private String lastCommittedTransaction;

	public Balance(BalanceSide balanceSide, Amount amount) {
		this.balanceSide = balanceSide;
		this.amount = amount;
	}
}

package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@NoArgsConstructor
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="ChartOfAccount_name_validFrom_unique")})
public class ChartOfAccount extends LedgerEntity {
	@Builder
	public ChartOfAccount(String id, String name, LocalDateTime validFrom, LocalDateTime created, String user) {
		super(id, name, validFrom, created, user);
	}
}
